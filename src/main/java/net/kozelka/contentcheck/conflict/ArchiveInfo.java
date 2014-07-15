package net.kozelka.contentcheck.conflict;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representation of an explored archive
 * @author Petr Kozelka
 */
public class ArchiveInfo {
    private String key;
    private int classCount;
    private Map<String, Conflict> conflicts = new LinkedHashMap<String, Conflict>();

    public void addConflict(ArchiveInfo otherArchive, ResourceInfo conflictingResource) {
        Conflict conflict = conflicts.get(otherArchive.getKey());
        if (conflict == null) {
            conflict = new Conflict(otherArchive);
            conflicts.put(otherArchive.getKey(), conflict);
        }
        conflict.addResource(conflictingResource);
    }

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

    public Collection<Conflict> getConflicts() {
        return conflicts.values();
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
}
