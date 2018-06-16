package com.lineate.xonix.mind.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServiceUtils {
    public static Path getVideoDir(Integer matchId, String videoDir) throws IOException {
        Path pathVideoDir = Paths.get(System.getProperty("user.dir"), videoDir, "match-" + matchId);
        if (!pathVideoDir.toFile().exists()) {
            Files.createDirectories(pathVideoDir);
        }
        return pathVideoDir;
    }

    public static Path getMvnLogDir(String mvnLogDir) throws IOException {
        Path pathVideoDir = Paths.get(System.getProperty("user.dir"), mvnLogDir);
        if (!pathVideoDir.toFile().exists()) {
            Files.createDirectories(pathVideoDir);
        }
        return pathVideoDir;
    }
}
