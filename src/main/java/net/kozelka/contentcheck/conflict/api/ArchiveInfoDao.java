package net.kozelka.contentcheck.conflict.api;

import java.util.Set;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * @author Petr Kozelka
 */
public interface ArchiveInfoDao {
    ArchiveInfo saveArchive(ArchiveInfo archiveInfo);

    Set<ArchiveInfo> getAllArchives();

    ResourceInfo saveResource(ResourceInfo resourceInfo);
}
