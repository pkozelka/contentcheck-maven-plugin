package net.kozelka.contentcheck.conflict.api;

import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;

/**
 * Represents the results of conflict checking.
 * @author Petr Kozelka
 */
public class ConflictCheckResponse {

    private final List<ArchiveInfo> exploredArchives = new ArrayList<ArchiveInfo>();
    private final List<ArchiveConflict> archiveConflicts = new ArrayList<ArchiveConflict>();

    public List<ArchiveConflict> getArchiveConflicts() {
        return archiveConflicts;
    }

    public List<ArchiveInfo> getExploredArchives() {
        return exploredArchives;
    }
}
