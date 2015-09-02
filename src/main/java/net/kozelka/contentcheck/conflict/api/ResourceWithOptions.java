package net.kozelka.contentcheck.conflict.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;

/**
 * Represents a resource, with all the candidate {@link ArchiveInfo archives} trying to supply it.
 *
 * @author Petr Kozelka
 */
public final class ResourceWithOptions {
    private String uri;
    private final Map<String, List<ArchiveInfo>> candidatesByHash = new HashMap<String, List<ArchiveInfo>>();
    private final List<ArchiveInfo> allCandidates = new ArrayList<ArchiveInfo>();

    public void addCandidate(String hash, ArchiveInfo archive) {
        allCandidates.add(archive);
        //
        List<ArchiveInfo> candidates = candidatesByHash.get(hash);
        if (candidates == null) {
            candidates = new ArrayList<ArchiveInfo>();
            candidatesByHash.put(hash, candidates);
        }
        candidates.add(archive);
    }

    public boolean hasConflicts() {
        return candidatesByHash.size() > 1;
    }

    public boolean hasOverlaps() {
        return allCandidates.size() > 1;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<ArchiveInfo> getAllCandidates() {
        return allCandidates;
    }

    public Map<String, List<ArchiveInfo>> getCandidatesByHash() {
        return candidatesByHash;
    }
}
