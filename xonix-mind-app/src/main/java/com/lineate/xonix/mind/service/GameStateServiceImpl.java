package com.lineate.xonix.mind.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import com.google.common.collect.ImmutableList;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.ModelGameState;
import com.lineate.xonix.mind.utils.ExternalCall;
import com.lineate.xonix.mind.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GameStateServiceImpl implements GameStateService {

    @Value("${xonix.default.log.video.dir}")
    String logVideoDir;

    @Value("${xonix.default.log.mvn.dir}")
    String mvnLogDir;

    @Value("${xonix.default.build.ffmpeg.command}")
    String ffmpegCommand;

    // keys are matchId
    private Map<Integer, ModelGameState> cache = new ConcurrentHashMap<>();
    private Map<Integer, AtomicInteger> imageCounter = new ConcurrentHashMap<>();

    private final ImmutableList<Color> colors = ImmutableList.of(
        Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.LIGHT_GRAY,
        Color.CYAN, Color.MAGENTA, Color.PINK, Color.ORANGE, Color.GRAY
    );

    @Override
    public void keepGameState(Integer matchId, ModelGameState state) {
        cache.put(matchId, state);
    }

    @Override
    public void clearGameState(Integer matchId) {
        cache.remove(matchId);
    }

    @Override
    public Optional<ModelGameState> takeGameState(Integer matchId) {
        return Optional.ofNullable(cache.get(matchId));
    }

    @Override
    public void createGameStateFrame(Path pathVideoDir, String gameState, Integer matchId, boolean useColors) throws ServiceException {
        try {
            int width = 1200;
            int height = 900;
            String imageType = "png";
            BufferedImage img = new BufferedImage(width, height,
                useColors? BufferedImage.TYPE_3BYTE_BGR: BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D graphics = img.createGraphics();
            graphics.setColor(Color.white);
            Font font = new Font("monospaced", Font.BOLD, 12);
            graphics.setFont(font);
            FontMetrics fm = graphics.getFontMetrics(font);
            val rec = fm.getStringBounds("A", graphics);
            int charWidth = (int) rec.getWidth();
            int charHeight = (int) rec.getHeight();

            int y = 18;
            int ln = 0;
            for (String line : gameState.split("\n")) {
                if (ln++ > 4 && useColors) {
                    Pattern regex = Pattern.compile("(.*?)(\\d)");
                    Matcher m = regex.matcher(line);
                    int x = 0;
                    int i = 0; // char counter
                    while (m.find()) {
                        int botId = Integer.parseInt(m.group(2));
                        // botId is in [0..9] range for sure
                        graphics.drawString(m.group(1), x, y);
                        x += charWidth * m.group(1).length();
                        graphics.setColor(colors.get(botId));
                        graphics.drawString(m.group(2), x, y);
                        x += charWidth;
                        graphics.setColor(Color.white);
                        i += m.group(0).length();
                    }
                    graphics.drawString(line.substring(i), x, y);
                } else {
                    graphics.drawString(line, 0, y);
                }
                y += charHeight + 10;
            }
            imageCounter.computeIfAbsent(matchId, integer -> new AtomicInteger(1));
            File file = new File(
                    pathVideoDir.toString() + "/a" + imageCounter.get(matchId).getAndIncrement()
                            + "." + imageType);
            ImageIO.write(img, imageType, file);

        } catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    private final FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".png");

    @Override
    public void createGameMatchVideo(Integer id, Path pathVideoDir) throws IOException {
        ExternalCall.call(pathVideoDir.toFile(), Arrays.asList(
                ffmpegCommand,
                "-y", "-r", "24", "-i", "a%d.png", "-an", "-vcodec", "libvpx", "./replay-" + id + ".webm"));
        for(File frame : Objects.requireNonNull(pathVideoDir.toFile()
                .listFiles(filter)))
            FileUtils.forceDelete(frame);
    }

    @Override
    public InputStream retrieveMatchReplay(Integer matchId) throws ServiceException {
        try{
            Path videoDir = ServiceUtils.getVideoDir(matchId, logVideoDir);
            return Files.newInputStream(Paths.get(videoDir.toString(),
                    "/replay-" + matchId + ".webm"));
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public InputStream retrieveMvnBotLog(String commitHash) throws ServiceException {
        try{
            Path mvnLogDir = ServiceUtils.getMvnLogDir(this.mvnLogDir);
            return Files.newInputStream(Paths.get(mvnLogDir.toString(), commitHash + ".txt"));
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }
}
