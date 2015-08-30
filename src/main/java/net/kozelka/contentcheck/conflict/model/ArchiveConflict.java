package net.kozelka.contentcheck.conflict.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold information about conflicting resources between two archives.
 * One instance represents pointer from one archive to another (conflicting) one, and keeps all resources on which a conflict is detected.
 *
 * @author Petr Kozelka
 */
public class ArchiveConflict {
    private ArchiveInfo thisArchive;
    private ArchiveInfo thatArchive;
    private final List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

    public void addResource(ResourceInfo ri) {
        resources.add(ri);
    }

    /**
     * @return the conflicting archive
     */
    public ArchiveInfo getThatArchive() {
        return thatArchive;
    }

    public void setThatArchive(ArchiveInfo thatArchive) {
        this.thatArchive = thatArchive;
    }

    /**
     * @return all conflicting resources in the {@link #getThatArchive() thatArchive}
     */
    public List<ResourceInfo> getResources() {
        return resources;
    }

    public ArchiveInfo getThisArchive() {
        return thisArchive;
    }

    public void setThisArchive(ArchiveInfo thisArchive) {
        this.thisArchive = thisArchive;
    }
}
