package net.kozelka.contentcheck.conflict.impl;

import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ConflictingArchive;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public class ConflictCheckResponsePrinter {
    public static int printResults(ConflictCheckResponse response, int previewThreshold, StreamConsumer output) {
        final List<ArchiveInfo> sortedConflictingArchives = new ArrayList<ArchiveInfo>(response.getConflictingArchives());
        Collections.sort(sortedConflictingArchives, new Comparator<ArchiveInfo>() {
            public int compare(ArchiveInfo o1, ArchiveInfo o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        int totalConflicts = 0;
        for (ArchiveInfo cai : sortedConflictingArchives) {
            output.consumeLine("");
            output.consumeLine(String.format("File '%s' (%d classes):", cai.getKey(), cai.getClassCount()));
            final List<ConflictingArchive> sortedConflicts = new ArrayList<ConflictingArchive>(cai.getConflictingArchives());
            Collections.sort(sortedConflicts, new Comparator<ConflictingArchive>() {
                public int compare(ConflictingArchive o1, ConflictingArchive o2) {
                    return o1.getArchiveInfo().getKey().compareTo(o2.getArchiveInfo().getKey());
                }
            });
            for (ConflictingArchive conflictingArchive : sortedConflicts) {
                final List<ResourceInfo> conflictResources = conflictingArchive.getResources();
                final int conflictResourceCount = conflictResources.size();
                totalConflicts += conflictResourceCount;
                output.consumeLine(String.format("%8d class conflicts with '%s'", conflictResourceCount, conflictingArchive.getArchiveInfo().getKey()));
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
        }
        output.consumeLine("-------------------------------------------------");
        output.consumeLine(String.format("Total: %d conflicts affect %d of %d archives.",
                totalConflicts,
                sortedConflictingArchives.size(),
                response.getExploredArchiveCount()));
        return totalConflicts;
    }
}
