package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * Collects unique {@link net.kozelka.contentcheck.conflict.api.ConflictCheckResponse.ArchiveConflict conflicts}
 * between pairs of {@link ArchiveInfo archives}.
 *
 * @author Petr Kozelka
 */
class ConflictCollector {
    private final Map<String, ConflictCheckResponse.ArchiveConflict> conflicts = new LinkedHashMap<String, ConflictCheckResponse.ArchiveConflict>();

    ConflictCheckResponse.ArchiveConflict save(ConflictCheckResponse.ArchiveConflict conflict) {
        final String key = conflict.getThisArchive().getKey() + "::" + conflict.getThatArchive().getKey();
        final ConflictCheckResponse.ArchiveConflict existingConflict = conflicts.get(key);
        if (existingConflict != null) {
            return existingConflict;
        }
        conflicts.put(key, conflict);
        return conflict;
    }

    public Collection<? extends ConflictCheckResponse.ArchiveConflict> getAll() {
        return conflicts.values();
    }

    public ConflictCheckResponse.ArchiveConflict addOverlap(ArchiveInfo thisArchive, ArchiveInfo thatArchive, ResourceInfo conflictingResource, boolean isDuplicate) {
        ConflictCheckResponse.ArchiveConflict archiveConflict = new ConflictCheckResponse.ArchiveConflict();
        archiveConflict.setThisArchive(thisArchive);
        archiveConflict.setThatArchive(thatArchive);
        archiveConflict = save(archiveConflict);
        archiveConflict.addOverlap(conflictingResource, isDuplicate);
        return archiveConflict;
    }

}
