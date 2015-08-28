package net.kozelka.contentcheck.conflict;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public class ResourceInfo {
    String key;
    private final List<ArchiveInfo> hostingArchives = new ArrayList<ArchiveInfo>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ArchiveInfo> getHostingArchives() {
        return hostingArchives;
    }

}
