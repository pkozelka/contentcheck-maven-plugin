package net.kozelka.contentcheck.conflict.api;

import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;

/**
 * Represents the results of conflict checking.
 * @author Petr Kozelka
 */
public class ConflictCheckResponse {

    private int exploredArchiveCount;
    private final List<ArchiveConflict> archiveConflicts = new ArrayList<ArchiveConflict>();

    public int getExploredArchiveCount() {
        return exploredArchiveCount;
    }

    public void setExploredArchiveCount(int exploredArchiveCount) {
        this.exploredArchiveCount = exploredArchiveCount;
    }

    public void incrementExploredArchiveCount() {
        exploredArchiveCount++;
    }

    public List<ArchiveConflict> getArchiveConflicts() {
        return archiveConflicts;
    }
}
