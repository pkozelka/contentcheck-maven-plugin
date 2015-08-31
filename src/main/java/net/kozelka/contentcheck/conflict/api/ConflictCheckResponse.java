package net.kozelka.contentcheck.conflict.api;

import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * Represents the results of conflict checking.
 *
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
     * Holds information about overlaping resources between two archives.
     * One instance represents pointer from one archive to another (conflicting) one, and keeps all resources on which an overlap is detected.
     *
     * @author Petr Kozelka
     */
    public static class ArchiveConflict {
        private ArchiveInfo thisArchive;
        private ArchiveInfo thatArchive;
        private final List<ResourceInfo> overlapingResources = new ArrayList<ResourceInfo>();
        private final List<ResourceInfo> conflictingResources = new ArrayList<ResourceInfo>();
        private final List<ResourceInfo> duplicateResources = new ArrayList<ResourceInfo>();

        /**
         * @return the conflicting archive
         */
        public ArchiveInfo getThatArchive() {
            return thatArchive;
        }

        public void setThatArchive(ArchiveInfo thatArchive) {
            this.thatArchive = thatArchive;
        }

        public ArchiveInfo getThisArchive() {
            return thisArchive;
        }

        public void setThisArchive(ArchiveInfo thisArchive) {
            this.thisArchive = thisArchive;
        }

        /**
         * Adds a resource on which an overlap was detected.
         * Each overlap is either {@link #addDuplicate(ResourceInfo) duplicate} or {@link #addConflict(ResourceInfo) conflict};
         * the caller is responsible for deciding which of them is it.
         * @param resource -
         * @param isDuplicate true for a <b>duplicate</b>, false for a <b>serious conflict</b>
         */
        public void addOverlap(ResourceInfo resource, boolean isDuplicate) {
            overlapingResources.add(resource);
            if (isDuplicate) {
                duplicateResources.add(resource);
            } else {
                conflictingResources.add(resource);
            }
        }

        /**
         * @return overlaping resources between {@link #getThisArchive() thisArchive} and {@link #getThatArchive() thatArchive}
         */
        public List<ResourceInfo> getOverlapingResources() {
            return overlapingResources;
        }

        public List<ResourceInfo> getConflictingResources() {
            return conflictingResources;
        }

        public List<ResourceInfo> getDuplicateResources() {
            return duplicateResources;
        }
    }
}
