package com.lineate.xonix.mind.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Slf4j
public final class ExternalCall {
    // TODO deduplicate these functions
    public static Boolean call(File tempDir, List<String> args) {
        try {
            val process = new ProcessBuilder(args)
                .redirectErrorStream(true)
                .directory(tempDir)
                .start();

            val t0 = System.currentTimeMillis();
            val br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            br.lines().forEach(log::info);
            val exitValue = process.waitFor();
            val t1 = System.currentTimeMillis();
            log.info("Process exited: " + exitValue + ", elapsed " + (t1 - t0) / 1000.0 + " seconds");
            return exitValue == 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public static Optional<String> callWithResult(File tempDir, List<String> args) {
        try {
            val process = new ProcessBuilder(args)
                .redirectErrorStream(true)
                .directory(tempDir)
                .start();

            val t0 = System.currentTimeMillis();
            val br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Optional<String> result = br.lines().findFirst();
            val exitValue = process.waitFor();
            val t1 = System.currentTimeMillis();
            log.info("Process exited: " + exitValue + ", elapsed " + (t1 - t0) / 1000.0 + " seconds");
            return exitValue == 0 ? result : Optional.empty();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }
}
