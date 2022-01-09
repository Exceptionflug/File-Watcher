package de.exceptionflug.filewatcher.watcher;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class ProcessWatcher {

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    private final Process process;
    private final String tag;

    public ProcessWatcher(Process process, String tag) {
        this.process = process;
        this.tag = tag;
        init();
    }

    private void init() {
        process.onExit().thenAccept(process -> {
            log.info("[" + tag + "] Process terminated with exit code " + process.exitValue());
        });
        grabStreamOutput(process.getInputStream(), false);
        grabStreamOutput(process.getErrorStream(), true);
    }

    private void grabStreamOutput(InputStream inputStream, boolean stderr) {
        EXECUTOR.execute(() -> {
            try (var bufferedInputStream = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                while (!Thread.interrupted() && process.isAlive()) {
                    var line = bufferedInputStream.readLine();
                    if (line == null) {
                        return;
                    }
                    if (stderr) {
                        log.error("[" + tag + "] " + line);
                    } else {
                        log.info("[" + tag + "] " + line);
                    }
                }
            } catch (IOException e) {
                log.error("Exception occurred while closing stream", e);
            }
        });
    }

}
