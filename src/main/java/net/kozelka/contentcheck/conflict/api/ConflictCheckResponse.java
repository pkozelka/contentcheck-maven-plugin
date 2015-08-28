package net.kozelka.contentcheck.conflict.api;

import net.kozelka.contentcheck.conflict.model.ArchiveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the results of conflict checking.
 * @author Petr Kozelka
 */
public class ConflictCheckResponse {

    private final List<ArchiveInfo> conflictingArchives = new ArrayList<ArchiveInfo>();
    private int exploredArchiveCount;

    public List<ArchiveInfo> getConflictingArchives() {
        return conflictingArchives;
    }

    public int getExploredArchiveCount() {
        return exploredArchiveCount;
    }

    public void setExploredArchiveCount(int exploredArchiveCount) {
        this.exploredArchiveCount = exploredArchiveCount;
    }

    public void incrementExploredArchiveCount() {
        exploredArchiveCount++;
    }
}
