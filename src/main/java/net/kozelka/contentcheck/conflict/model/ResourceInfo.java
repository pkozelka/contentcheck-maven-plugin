package net.kozelka.contentcheck.conflict.model;

/**
 * Represents one resource in the classpath element - for instance, a class file.
 */
public class ResourceInfo {
    private String uri;
    private String hash;

    public ResourceInfo() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
