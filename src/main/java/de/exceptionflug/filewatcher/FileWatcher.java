package de.exceptionflug.filewatcher;

import de.exceptionflug.filewatcher.scanner.DirectoryScanner;
import de.exceptionflug.filewatcher.watcher.ProcessWatcher;
import de.exceptionflug.filewatcher.watcher.Watcher;
import de.leonhard.storage.Yaml;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

@Slf4j
public final class FileWatcher {

    private final Map<String, String[]> actions = new HashMap<>();
    private final List<Watcher> watchers = new ArrayList<>();
    private final Yaml config;

    public FileWatcher(Yaml config) {
        this.config = config;
        init();
    }

    private void init() {
        for (String action : config.singleLayerKeySet("actions")) {
            actions.put(action, config.getList("actions." + action).toArray(new String[0]));
        }
        for (String key : config.singleLayerKeySet("scanners")) {
            var scanner = new DirectoryScanner(new File(config.getOrSetDefault("scanners." + key + ".path", "./")));
            scanner.recursive(config.getOrSetDefault("scanners." + key + ".recursive", false));
            log.info("Registered scanner for " + scanner.directory().getAbsolutePath());
            watchers.add(new Watcher(scanner, config.getOrSetDefault("scanners." + key + ".action", "default"),
                    config.get("scanners." + key + ".fileFilter", null)));
        }
        createTimer();
    }

    private void createTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Watcher watcher : watchers) {
                    try {
                        evalWatcher(watcher);
                    } catch (Exception exception) {
                        log.error("Exception occurred while evaluating watcher with path " + watcher.scanner().directory().getAbsolutePath());
                    }
                }
            }
        }, 1000, 1000);
    }

    private void evalWatcher(Watcher watcher) {
        watcher.scanner().scanNewFiles(pathname -> {
            if (watcher.regExp() != null) {
                return pathname.getName().matches(watcher.regExp());
            }
            return true;
        }).forEach(file -> handleNewFile(watcher, file));
    }

    private void handleNewFile(Watcher watcher, File file) {
        log.info("Detected new file: " + file.getAbsolutePath());
        var command = actions.get(watcher.action());
        if (command == null) {
            log.warn("Unable to perform unknown action " + watcher.action() + " for file " + file.getAbsolutePath());
            return;
        }
        command = new String[command.length];
        System.arraycopy(actions.get(watcher.action()), 0, command, 0, command.length);
        formatCommand(command, file);
        try {
            log.info("[" + file.getName() + "] Running: " + Arrays.toString(command));
            new ProcessWatcher(Runtime.getRuntime().exec(command), file.getName());
        } catch (Exception e) {
            log.error("Unable to perform action " + watcher.action(), e);
        }
    }

    private void formatCommand(String[] command, File file) {
        for (int i = 0; i < command.length; i++) {
            String cmd = command[i];
            command[i] = String.format(cmd, file.getAbsolutePath());
        }
    }

}
