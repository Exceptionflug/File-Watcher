package de.exceptionflug.filewatcher.scanner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@EqualsAndHashCode
@Accessors(fluent = true)
public class DirectoryScanner {

    @Getter
    private final File directory;
    private final long initialized = System.currentTimeMillis();
    private final List<String> filesToSkip = new ArrayList<>();
    @Getter
    @Setter
    private boolean recursive;

    public DirectoryScanner(File directory) {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException(directory == null ? "null" : directory.getAbsolutePath() + " must be a directory");
        }
        this.directory = directory;
    }

    public List<File> scanNewFiles(FileFilter fileFilter) {
        return scanNewFiles(directory, fileFilter);
    }

    private List<File> scanNewFiles(File directory, FileFilter fileFilter) {
        var out = new ArrayList<File>();
        for (File file : Objects.requireNonNull(directory.listFiles(fileFilter))) {
            if (filesToSkip.contains(file.getAbsolutePath())) {
                continue;
            }
            if (file.isDirectory()) {
                if (recursive) {
                    out.addAll(scanNewFiles(file, fileFilter));
                }
                continue;
            }
            if (processFile(file)) {
                out.add(file);
                filesToSkip.add(file.getAbsolutePath());
            }
        }
        return out;
    }

    private boolean processFile(File file) {
        try {
            var attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long created = attributes.creationTime().to(TimeUnit.MILLISECONDS);
            return created > initialized;
        } catch (Exception e) {
            filesToSkip.add(file.getAbsolutePath());
            log.error("Unable to process " + file.getAbsolutePath(), e);
            return false;
        }
    }

}
