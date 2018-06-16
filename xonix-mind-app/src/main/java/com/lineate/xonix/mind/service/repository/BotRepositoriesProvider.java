package com.lineate.xonix.mind.service.repository;

import com.lineate.xonix.mind.exception.RepositoryException;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BotClassLoader;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.service.BotProvider;
import com.lineate.xonix.mind.utils.ExternalCall;
import com.lineate.xonix.mind.utils.ReflectionUtils;
import com.lineate.xonix.mind.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Slf4j
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BotRepositoriesProvider implements BotProvider {

    private static final String START_CLASS = "Start-Class";

    @Value("${xonix.default.log.repositories.dir}")
    String repoDir;
    @Value("${xonix.default.log.mvn.dir}")
    String mvnLogDir;
    @Value("${xonix.default.build.git.command}")
    String gitCommand;
    @Value("${xonix.default.build.mvn.command}")
    String mvnCommand;
    private Map<String, BotClassLoader> botClassLoaders = new ConcurrentHashMap<>();

    @Override
    public List<Bot> retrieve(List<URL> botRepoUrls, BuildParam buildParam) throws ServiceException {
        List<Bot> bots = new ArrayList<>();
        Map<String, BotClassLoader> botClassLoader = getBotClassLoader(botRepoUrls, buildParam);
        botClassLoaders.putAll(botClassLoader);
        botClassLoaders.forEach((hash, botCL) -> {
            try {
                URL jarURL = botCL.getUrl();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL},
                        this.getClass().getClassLoader());
                botCL.setUrlClassLoader(classLoader);
            } catch (Exception e) {
                //TODO: Collect exception for every bot and return user
                log.error(e.getMessage(), e);
            }
        });

        //TODO: Implementation of Bot is dangerous external object!!!!
        botClassLoaders.forEach((hash, botCl) -> {
            try {
                Bot instance = ReflectionUtils.newInstance(botCl.getClassName(), botCl.getUrlClassLoader());
                bots.add(instance);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                //TODO: Collect exception for every bot and return user
                log.error(e.getMessage(), e);
            }
        });

        return bots;
    }

    @Override
    public Optional<Bot> get(URL botUrl) throws ServiceException {
        try {
            String hash = getUrlHash(botUrl);
            BotClassLoader botCl = botClassLoaders.get(hash);
            String className = botCl.getClassName();
            URLClassLoader urlClassLoader = botCl.getUrlClassLoader();
            return Optional.ofNullable(ReflectionUtils.newInstance(className, urlClassLoader));
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            //TODO: Collect exception for every bot and return user
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private Map<String, BotClassLoader> getBotClassLoader(List<URL> botRepoUrls, BuildParam buildParam) throws ServiceException {
        val botClassLoaders = new HashMap<String, BotClassLoader>();
        for (URL url : botRepoUrls) {
            try {
                Path repoPath = getRepoPath(url);
                if (repoPath.toFile().exists() && buildParam.isSkipBuild()) {
                    // just load jar without git check and maven
                    val projectDirectory = FilenameUtils.getName(url.getFile());
                    val botPath = Paths.get(repoPath.toString(), projectDirectory);
                    val targetDir = new File(botPath.toFile(), "target");
                    val pair = loadResultJar(url, targetDir);
                    botClassLoaders.put(pair.getLeft(), pair.getRight());
                } else if (repoPath.toFile().exists()) {
                    // bot source has been cached in something like
                    // ./xonix-mind/repositories/43418bcd659ae9c042022584c4d46238
                    val projectDirectory = FilenameUtils.getName(url.getFile());
                    val botPath = Paths.get(repoPath.toString(), projectDirectory);
                    String localHeadHash = getLocalHeadHash(botPath);
                    val repo = url.getProtocol().equals("file") ? url.getFile() : url.toString();
                    String remoteHeadHash = ExternalCall.callWithResult(botPath.toFile(),
                            Arrays.asList(gitCommand, "ls-remote", repo, "HEAD"))
                            .orElseThrow(() -> new RuntimeException("Local copy is broken: " + repo))
                            .split("\\s")[0].substring(0, 7);
                    if (!localHeadHash.equals(remoteHeadHash)) {
                        gitMirror(url, botPath);
                        mvnPackage(botPath.toFile());
                    }
                    val targetDir = new File(botPath.toFile(), "target");
                    if (getResultJar(targetDir) == null) {
                        mvnPackage(targetDir.getParentFile());
                    }
                    val pair = loadResultJar(url, targetDir);
                    botClassLoaders.put(pair.getLeft(), pair.getRight());
                } else {
                    //TODO: Clone and build in different thread
                    val projectDir = gitClone(url, repoPath);
                    val targetDir = mvnPackage(projectDir);
                    val pair = loadResultJar(url, targetDir);
                    botClassLoaders.put(pair.getLeft(), pair.getRight());
                }
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }
        return botClassLoaders;
    }

    @Override
    public String getLocalHeadHashByUrl(URL url) throws ServiceException {
        try {
            Path repoPath = getRepoPath(url);
            if (repoPath.toFile().exists()) {
                val projectDirectory = FilenameUtils.getName(url.getFile());
                val botPath = Paths.get(repoPath.toString(), projectDirectory);
                return getLocalHeadHash(botPath);
            } else {
                throw new ServiceException("Cannot find repository by url: " + url.toString());
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    private String getLocalHeadHash(Path botPath) throws Exception {
        return ExternalCall.callWithResult(botPath.toFile(),
                Arrays.asList(gitCommand, "rev-parse", "--short", "HEAD"))
                .orElseThrow(() -> new Exception("Local copy is broken: " + botPath.toFile()));
    }

    private Path getRepoPath(URL url) throws IOException {
        Path localRepoDir = Paths.get(repoDir);
        if (!localRepoDir.toFile().exists()) {
            Files.createDirectory(localRepoDir);
        }
        return Paths.get(localRepoDir.toString(), getUrlHash(url));
    }

    private File mvnPackage(File projectDir) throws Exception {
        String localHeadHash = getLocalHeadHash(Paths.get(projectDir.toString()));
        String mvnLog = ServiceUtils.getMvnLogDir(mvnLogDir).toString();
        val mvnCommand = Arrays.asList(
                this.mvnCommand, "clean", "package", "--log-file", String.format("%s/%s.txt", mvnLog, localHeadHash));
        val result = ExternalCall.call(projectDir, mvnCommand);
        if (result) {
            return new File(projectDir, "target");
        } else {
            throw new RepositoryException("Cannot run maven in " + projectDir);
        }
    }

    private File getResultJar(File targetDir) {
        File[] jarFiles = targetDir.listFiles((dir, name) ->
                name.endsWith(".jar") && name.startsWith("result-"));
        return (jarFiles == null || ArrayUtils.isEmpty(jarFiles)) ? null : jarFiles[0];
    }

    private Pair<String, BotClassLoader> loadResultJar(URL url, File targetDir) throws Exception {
        File jar = getResultJar(targetDir);
        if (jar == null) {
            throw new RepositoryException("Cannot find result jar in: " + targetDir);
        }
        Manifest manifest = new JarFile(jar).getManifest();
        Attributes attributes = manifest.getMainAttributes();
        if (attributes != null) {
            String className = attributes.getValue(START_CLASS);
            if (StringUtils.isNoneBlank(className)) {
                String repoHash = getUrlHash(url);
                return Pair.of(repoHash, BotClassLoader.builder()
                        .url(jar.toURI().toURL())
                        .className(className)
                        .build());
            }
        }
        throw new RepositoryException("Cannot load jar in " + targetDir);
    }

    private File gitClone(URL url, Path pathBotDir) throws RepositoryException {
        try {
            Files.createDirectory(pathBotDir);
            val repo = url.getProtocol().equals("file") ? url.getFile() : url.toString();
            val result = ExternalCall.call(pathBotDir.toFile(),
                    Arrays.asList(gitCommand, "clone", repo));
            if (!result) {
                throw new RepositoryException("Git clone: " + repo);
            }
            String projectDirectory = FilenameUtils.getName(url.getFile());
            return Paths.get(pathBotDir.toString(), projectDirectory).toFile();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void gitMirror(URL url, Path pathBotDir) throws RepositoryException {
        // git checkout master
        // git fetch origin
        // git reset --hard origin/master
        val repo = url.getProtocol().equals("file") ? url.getFile() : url.toString();
        val r1 = ExternalCall.call(pathBotDir.toFile(),
                Arrays.asList(gitCommand, "checkout", "master"));
        val r2 = ExternalCall.call(pathBotDir.toFile(),
                Arrays.asList(gitCommand, "fetch", "origin"));
        val r3 = ExternalCall.call(pathBotDir.toFile(),
                Arrays.asList(gitCommand, "reset", "--hard", "origin/master"));
        if (!r1 || !r2 || !r3) {
            throw new RepositoryException("Git fetch and reset failed: " + repo);
        }
    }

    private String getUrlHash(URL url) {
        return DigestUtils.md5DigestAsHex(url.toString().getBytes());
    }
}
