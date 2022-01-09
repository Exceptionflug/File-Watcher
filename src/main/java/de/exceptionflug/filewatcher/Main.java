package de.exceptionflug.filewatcher;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Getter
@Accessors(fluent = true)
public class Main {

    private static FileWatcher fileWatcher;

    public static void main(String[] args) throws IOException {
        log.info("FileWatcher by Nico Britze");
        createDefaultConfigIfNecessary();
        fileWatcher = new FileWatcher(new Yaml(new File("config.yml")));
    }

    private static void createDefaultConfigIfNecessary() throws IOException {
        File file = new File("config.yml");
        if (file.exists()) {
            return;
        }
        file.createNewFile();
        log.info("Created default configuration file");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(Main.class.getResourceAsStream("/config.yml").readAllBytes());
            fileOutputStream.flush();
        }
    }

}
