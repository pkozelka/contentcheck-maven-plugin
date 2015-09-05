package net.kozelka.contentcheck.mojo;

import net.kozelka.contentcheck.expect.impl.ContentChecker;
import org.apache.maven.plugin.logging.Log;
import java.io.File;

/**
 * @author Petr Kozelka
 */
public class MyContentCheckerListener implements ContentChecker.Events {
    private final Log log;

    public MyContentCheckerListener(Log log) {
        this.log = log;
    }

    public void summary(File sourceFile, int checkedCount, int totalCount) {
        log.info(String.format("'%s' contains %d checked and %d total files", sourceFile, checkedCount, totalCount));

    }

    public void duplicate(File listingFile, String line) {
        log.warn("The listing file " + listingFile + "  defines duplicate entry " + line);
    }

    public void contentListingSummary(File listingFile, int pathCount, int totalCount) {
        log.info(String.format("Content listing file '%s' contains %d paths on %d total lines", listingFile, pathCount, totalCount));
    }
}
