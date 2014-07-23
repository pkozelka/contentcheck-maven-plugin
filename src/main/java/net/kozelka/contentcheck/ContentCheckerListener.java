package net.kozelka.contentcheck;

import java.io.File;

/**
 * @author Petr Kozelka
 */
public interface ContentCheckerListener {
    void summary(File sourceFile, int checkedCount, int totalCount);

    void duplicate(File listingFile, String line);

    void contentListingSummary(File listingFile, int pathCount, int totalCount);
}
