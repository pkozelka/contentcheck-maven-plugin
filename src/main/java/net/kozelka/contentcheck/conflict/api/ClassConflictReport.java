package net.kozelka.contentcheck.conflict.api;

import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;

/**
 * Represents the results of conflict checking as prepared by {@link net.kozelka.contentcheck.conflict.impl.ClassConflictAnalyzer}.
 *
 * @author Petr Kozelka
 */
public final class ClassConflictReport {

    private final List<ArchiveInfo> exploredArchives = new ArrayList<ArchiveInfo>();
    private final List<ArchiveConflict> archiveConflicts = new ArrayList<ArchiveConflict>();
    private final List<ResourceWithOptions> resources = new ArrayList<ResourceWithOptions>();
    private int totalOverlaps;

    public List<ArchiveConflict> getArchiveConflicts() {
        return archiveConflicts;
    }

    public List<ArchiveInfo> getExploredArchives() {
        return exploredArchives;
    }

    public List<ResourceWithOptions> getResources() {
        return resources;
    }

    public void setTotalOverlaps(int totalOverlaps) {
        this.totalOverlaps = totalOverlaps;
    }

    public int getTotalOverlaps() {
        return totalOverlaps;
    }

}
