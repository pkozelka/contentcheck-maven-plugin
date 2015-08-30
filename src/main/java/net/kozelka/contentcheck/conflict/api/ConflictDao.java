package net.kozelka.contentcheck.conflict.api;

import java.util.Collection;

/**
 * @author Petr Kozelka
 */
public interface ConflictDao {

    /**
     * Saves conflict information as either new instance, or looks up an existing one.
     * Caller should then work only with the returned instance.
     *
     * @param conflict the new instance, candidate for saving
     * @return existing instance with the same pair of conflicting archives, or the new one
     */
    ConflictCheckResponse.ArchiveConflict save(ConflictCheckResponse.ArchiveConflict conflict);

    Collection<? extends ConflictCheckResponse.ArchiveConflict> getAll();
}
