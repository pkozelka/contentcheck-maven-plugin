package net.kozelka.contentcheck.conflict.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ArchiveConflict;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Prints the {@link ClassConflictReport results} of {@link ClassConflictAnalyzer#findConflicts() conflict detection} into console
 *
 * @author Petr Kozelka
 */
public class ClassConflictPrinter {
    private StreamConsumer output;
    private int previewThreshold = -1;

    public void setOutput(StreamConsumer output) {
        this.output = output;
    }

    public void setPreviewThreshold(int previewThreshold) {
        this.previewThreshold = previewThreshold;
    }

    public void print(ClassConflictReport report) {
        final List<ArchiveConflict> sortedArchiveConflicts = new ArrayList<ArchiveConflict>(report.getArchiveConflicts());
        Collections.sort(sortedArchiveConflicts, new Comparator<ArchiveConflict>() {
            public int compare(ArchiveConflict o1, ArchiveConflict o2) {
                int rv = o1.getThisArchive().getKey().compareTo(o2.getThisArchive().getKey());
                if (rv == 0) {
                    rv = o1.getThatArchive().getKey().compareTo(o2.getThatArchive().getKey());
                }
                return rv;
            }
        });

        String previousThis = "-";
        for (ArchiveConflict archiveConflict : sortedArchiveConflicts) {
            final String thisArchiveKey = archiveConflict.getThisArchive().getKey();
            if (!thisArchiveKey.equals(previousThis)) {
                output.consumeLine("");
                output.consumeLine(String.format("File '%s' (%d classes):", thisArchiveKey, archiveConflict.getThisArchive().getClassCount()));
                previousThis = thisArchiveKey;
            }

            final List<ResourceInfo> conflictResources = archiveConflict.getOverlapingResources();
            final int conflictResourceCount = conflictResources.size();
            output.consumeLine(String.format("%8d classes overlap (%d conflicts, %d duplicates) with '%s'",
                conflictResourceCount,
                archiveConflict.getConflictingResources().size(),
                archiveConflict.getDuplicateResources().size(),
                archiveConflict.getThatArchive().getKey()));
            if (previewThreshold == 0) continue;
            int cnt = 0;
            for (ResourceInfo resource : conflictResources) {
                cnt ++;
                if (cnt > previewThreshold && previewThreshold >= 0) {
                    output.consumeLine("                ...");
                    break;
                }
                output.consumeLine(String.format("                %s", resource.getUri()));
            }
        }
        output.consumeLine("-------------------------------------------------");
        output.consumeLine(String.format("Total: %d overlaps affect %d of %d archives.",
            report.getTotalOverlaps(),
            sortedArchiveConflicts.size(),
            report.getExploredArchives().size()));
    }
}
