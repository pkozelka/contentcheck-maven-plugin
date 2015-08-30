package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.api.ConflictDao;

/**
 * @author Petr Kozelka
 */
public class ConflictDaoImpl implements ConflictDao {
    private final Map<String, ConflictCheckResponse.ArchiveConflict> conflicts = new LinkedHashMap<String, ConflictCheckResponse.ArchiveConflict>();
    @Override
    public ConflictCheckResponse.ArchiveConflict save(ConflictCheckResponse.ArchiveConflict conflict) {
        final String key = conflict.getThisArchive().getKey() + "::" + conflict.getThatArchive().getKey();
        final ConflictCheckResponse.ArchiveConflict existingConflict = conflicts.get(key);
        if (existingConflict != null) {
            return existingConflict;
        }
        conflicts.put(key, conflict);
        return conflict;
    }

    @Override
    public Collection<? extends ConflictCheckResponse.ArchiveConflict> getAll() {
        return conflicts.values();
    }
}
