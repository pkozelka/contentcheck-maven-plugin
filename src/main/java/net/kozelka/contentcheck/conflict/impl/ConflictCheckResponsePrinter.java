package net.kozelka.contentcheck.conflict.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Prints the {@link ConflictCheckResponse results} of {@link ClassConflictDetector#findConflicts() conflict detection} into console
 *
 * @author Petr Kozelka
 */
public class ConflictCheckResponsePrinter {
    private StreamConsumer output;

    public void setOutput(StreamConsumer output) {
        this.output = output;
    }

    public void printResults(ConflictCheckResponse response, int previewThreshold) {
        final List<ConflictCheckResponse.ArchiveConflict> sortedArchiveConflicts = new ArrayList<ConflictCheckResponse.ArchiveConflict>(response.getArchiveConflicts());
        Collections.sort(sortedArchiveConflicts, new Comparator<ConflictCheckResponse.ArchiveConflict>() {
            public int compare(ConflictCheckResponse.ArchiveConflict o1, ConflictCheckResponse.ArchiveConflict o2) {
                int rv = o1.getThisArchive().getKey().compareTo(o2.getThisArchive().getKey());
                if (rv == 0) {
                    rv = o1.getThatArchive().getKey().compareTo(o2.getThatArchive().getKey());
                }
                return rv;
            }
        });

        String previousThis = "-";
        for (ConflictCheckResponse.ArchiveConflict archiveConflict : sortedArchiveConflicts) {
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
                response.getTotalOverlaps(),
                sortedArchiveConflicts.size(),
                response.getExploredArchives().size()));

        for (ResourceWithOptions rwo : response.getResources()) {
            if (! rwo.hasConflicts()) continue;
            output.consumeLine(rwo.uri);
            for (Map.Entry<String, List<ArchiveInfo>> entry : rwo.candidatesByHash.entrySet()) {
                output.consumeLine(String.format("  - %s: %s", entry.getKey(), entry.getValue()));

            }

        }
    }
}
