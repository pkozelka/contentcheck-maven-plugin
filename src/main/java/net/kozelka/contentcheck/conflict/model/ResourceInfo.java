package net.kozelka.contentcheck.conflict.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one resource in the classpath element - for instance, a class file.
 */
public class ResourceInfo {
    private String uri;
    private final List<ArchiveInfo> hostingArchives = new ArrayList<ArchiveInfo>();
    private String hash;


    public ResourceInfo() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<ArchiveInfo> getHostingArchives() {
        return hostingArchives;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
