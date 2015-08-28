package net.kozelka.contentcheck.conflict.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Kozelka
 */
public class ResourceInfo {
    private String key;
    private final List<ArchiveInfo> hostingArchives = new ArrayList<ArchiveInfo>();


    public ResourceInfo() {
    }

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
