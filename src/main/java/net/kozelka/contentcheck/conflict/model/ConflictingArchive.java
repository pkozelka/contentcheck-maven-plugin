package net.kozelka.contentcheck.conflict.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold information about conflicting resources between two archives.
 * One instance represents pointer from one archive to another (conflicting) one, and keeps all resources on which a conflict is detected.
 *
 *  @todo: this will be an internal linking, not an explicit entity
 * @author Petr Kozelka
 */
public class ConflictingArchive {
    private ArchiveInfo archiveInfo;
    private final List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

    public void setConflictingArchive(ArchiveInfo archiveInfo) {
        this.archiveInfo = archiveInfo;
    }

    public void addResource(ResourceInfo ri) {
        resources.add(ri);
    }

    /**
     * @return the conflicting archive
     */
    public ArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }

    /**
     * @return all conflicting resources in the {@link #getArchiveInfo() archiveInfo}
     */
    public List<ResourceInfo> getResources() {
        return resources;
    }
}
