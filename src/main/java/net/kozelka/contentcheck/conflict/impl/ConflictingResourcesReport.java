package net.kozelka.contentcheck.conflict.impl;

import java.util.List;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.api.ResourceWithOptions;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Petr Kozelka
 */
public class ConflictingResourcesReport {
    private StreamConsumer output;

    public void setOutput(StreamConsumer output) {
        this.output = output;
    }

    public void print(ClassConflictReport report) {
        for (ResourceWithOptions rwo : report.getResources()) {
            if (! rwo.hasConflicts()) continue;
            output.consumeLine(rwo.getUri());
            for (Map.Entry<String, List<ArchiveInfo>> entry : rwo.getCandidatesByHash().entrySet()) {
                output.consumeLine(String.format("  - %s: %s", entry.getKey(), entry.getValue()));
            }
        }
    }
}
