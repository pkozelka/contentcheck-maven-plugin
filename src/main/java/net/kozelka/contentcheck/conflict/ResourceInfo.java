package net.kozelka.contentcheck.conflict;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public class ResourceInfo {
    String key;
    private final List<ArchiveInfo> hostingArchives = new ArrayList<ArchiveInfo>();

    public void addHostingArchive(ArchiveInfo archiveInfo) {
        for (ArchiveInfo hostingArchive : hostingArchives) {
            hostingArchive.addConflict(archiveInfo, this);
            archiveInfo.addConflict(hostingArchive, this);
        }
        hostingArchives.add(archiveInfo);
    }
}
