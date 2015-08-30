package net.kozelka.contentcheck.conflict.api;

import java.util.Collection;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;

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
    ArchiveConflict save(ArchiveConflict conflict);

    Collection<? extends ArchiveConflict> getAll();
}
