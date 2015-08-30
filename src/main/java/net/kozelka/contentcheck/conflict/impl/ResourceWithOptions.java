package net.kozelka.contentcheck.conflict.impl;

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
class ResourceWithOptions {
    String uri;
    final Map<String, List<ArchiveInfo>> candidatesByHash = new HashMap<String, List<ArchiveInfo>>();
    final List<ArchiveInfo> allCandidates = new ArrayList<ArchiveInfo>();

    void addCandidate(String hash, ArchiveInfo archive) {
        allCandidates.add(archive);
        //
        List<ArchiveInfo> candidates = candidatesByHash.get(hash);
        if (candidates == null) {
            candidates = new ArrayList<ArchiveInfo>();
            candidatesByHash.put(hash, candidates);
        }
        candidates.add(archive);
    }

    boolean hasConflicts() {
        return candidatesByHash.size() > 1;
    }

    boolean hasOverlaps() {
        return allCandidates.size() > 1;
    }
}
