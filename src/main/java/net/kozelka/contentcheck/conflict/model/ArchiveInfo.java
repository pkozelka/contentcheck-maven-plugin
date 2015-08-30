package net.kozelka.contentcheck.conflict.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one classpath element, typically an archive
 * @author Petr Kozelka
 */
public class ArchiveInfo {
    private String key;
    private int classCount;
    private int resourceCount;
    private final List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

    /**
     * @return name of the archive, in presentation-specific form
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }

    /**
     * @return number of <code>.class</code> resources
     */
    public int getClassCount() {
        return classCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ArchiveInfo that = (ArchiveInfo) o;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
//
        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    /**
     * @return (read-only) list of resources in the archive
     * @see #addResource(ResourceInfo)
     */
    public List<ResourceInfo> getResources() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Adds resource to the list of archive resources.
     * @param resource -
     */
    public void addResource(ResourceInfo resource) {
        resources.add(resource);
        resourceCount++;
        final String name = resource.getUri();
        if (name.endsWith(".class")) {
            classCount++;
        }
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }
}
