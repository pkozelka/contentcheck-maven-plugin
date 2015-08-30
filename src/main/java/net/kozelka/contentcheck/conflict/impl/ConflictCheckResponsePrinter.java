package net.kozelka.contentcheck.conflict.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Petr Kozelka
 */
public class ConflictCheckResponsePrinter {
    public static int printResults(ConflictCheckResponse response, int previewThreshold, StreamConsumer output) {
        final List<ArchiveConflict> sortedArchiveConflicts = new ArrayList<ArchiveConflict>(response.getArchiveConflicts());
        Collections.sort(sortedArchiveConflicts, new Comparator<ArchiveConflict>() {
            public int compare(ArchiveConflict o1, ArchiveConflict o2) {
                int rv = o1.getThisArchive().getKey().compareTo(o2.getThisArchive().getKey());
                if (rv == 0) {
                    rv = o1.getThatArchive().getKey().compareTo(o2.getThatArchive().getKey());
                }
                return rv;
            }
        });
        int totalConflicts = 0;

        String previousThis = "-";
        for (ArchiveConflict archiveConflict : sortedArchiveConflicts) {
            final String thisArchiveKey = archiveConflict.getThisArchive().getKey();
            if (!thisArchiveKey.equals(previousThis)) {
                output.consumeLine("");
                output.consumeLine(String.format("File '%s' (%d classes):", thisArchiveKey, archiveConflict.getThisArchive().getClassCount()));
                previousThis = thisArchiveKey;
            }

            final List<ResourceInfo> conflictResources = archiveConflict.getResources();
            final int conflictResourceCount = conflictResources.size();
            totalConflicts += conflictResourceCount;
            output.consumeLine(String.format("%8d class conflicts with '%s'", conflictResourceCount, archiveConflict.getThatArchive().getKey()));
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
        output.consumeLine(String.format("Total: %d conflicts affect %d of %d archives.",
                totalConflicts,
                sortedArchiveConflicts.size(),
                response.getExploredArchiveCount()));
        return totalConflicts;
    }
}
