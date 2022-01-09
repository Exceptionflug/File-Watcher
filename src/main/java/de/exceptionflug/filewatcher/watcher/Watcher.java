package de.exceptionflug.filewatcher.watcher;

import de.exceptionflug.filewatcher.scanner.DirectoryScanner;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class Watcher {

    private final DirectoryScanner scanner;
    private final String action;
    private final String regExp;

    public Watcher(DirectoryScanner scanner, String action, String regExp) {
        this.scanner = scanner;
        this.action = action;
        this.regExp = regExp;
    }

}
