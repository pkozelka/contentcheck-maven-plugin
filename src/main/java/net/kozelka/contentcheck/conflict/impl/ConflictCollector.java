package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ArchiveConflict;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * Collects unique {@link ArchiveConflict conflicts}
 * between pairs of {@link ArchiveInfo archives}.
 *
 * @author Petr Kozelka
 */
class ConflictCollector {
    private final Map<String, ArchiveConflict> conflicts = new LinkedHashMap<String, ArchiveConflict>();

    ArchiveConflict save(ArchiveConflict conflict) {
        final String key = conflict.getThisArchive().getKey() + "::" + conflict.getThatArchive().getKey();
        final ArchiveConflict existingConflict = conflicts.get(key);
        if (existingConflict != null) {
            return existingConflict;
        }
        conflicts.put(key, conflict);
        return conflict;
    }

    public Collection<? extends ArchiveConflict> getAll() {
        return conflicts.values();
    }

    public ArchiveConflict addOverlap(ArchiveInfo thisArchive, ArchiveInfo thatArchive, ResourceInfo conflictingResource, boolean isDuplicate) {
        ArchiveConflict archiveConflict = new ArchiveConflict();
        archiveConflict.setThisArchive(thisArchive);
        archiveConflict.setThatArchive(thatArchive);
        archiveConflict = save(archiveConflict);
        archiveConflict.addOverlap(conflictingResource, isDuplicate);
        return archiveConflict;
    }

}
