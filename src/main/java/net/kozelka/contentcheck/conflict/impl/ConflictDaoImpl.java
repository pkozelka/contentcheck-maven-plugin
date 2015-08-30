package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ConflictDao;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;

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

    @Override
    public Collection<? extends ArchiveConflict> getAll() {
        return conflicts.values();
    }
}
