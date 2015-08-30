package net.kozelka.contentcheck.conflict.api;

import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * Represents the results of conflict checking.
 * @author Petr Kozelka
 */
public class ConflictCheckResponse {

    private final List<ArchiveInfo> exploredArchives = new ArrayList<ArchiveInfo>();
    private final List<ArchiveConflict> archiveConflicts = new ArrayList<ArchiveConflict>();

    public List<ArchiveConflict> getArchiveConflicts() {
        return archiveConflicts;
    }

    public List<ArchiveInfo> getExploredArchives() {
        return exploredArchives;
    }

    /**
     * Holds information about conflicting resources between two archives.
     * One instance represents pointer from one archive to another (conflicting) one, and keeps all resources on which a conflict is detected.
     *
     * @author Petr Kozelka
     */
    public static class ArchiveConflict {
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
}
