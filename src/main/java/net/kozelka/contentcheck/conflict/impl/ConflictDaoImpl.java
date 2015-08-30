package net.kozelka.contentcheck.conflict.impl;

import net.kozelka.contentcheck.conflict.api.ConflictDao;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Petr Kozelka
 */
public class ConflictDaoImpl implements ConflictDao {
    private final Map<String, ArchiveConflict> conflicts = new LinkedHashMap<String, ArchiveConflict>();
    @Override
    public ArchiveConflict save(ArchiveConflict conflict) {
        final String key = conflict.getThisArchive().getKey() + "::" + conflict.getThatArchive().getKey();
        final ArchiveConflict existingConflict = conflicts.get(key);
        if (existingConflict != null) {
            return existingConflict;
        }
        conflicts.put(key, conflict);
        return conflict;
    }
}
