package net.sf.buildbox.contentcheck.conflict;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold information about conflicting resources between two archives.
 * One instance represents pointer from one archive to another (conflicting) one, and keeps all resources on which a conflict is detected.
 *
 * @author Petr Kozelka
 */
public class Conflict {
    final ArchiveInfo otherArchive;
    final List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

    Conflict(ArchiveInfo otherArchive) {
        this.otherArchive = otherArchive;
    }

    public void addResource(ResourceInfo ri) {
        resources.add(ri);
    }
}
