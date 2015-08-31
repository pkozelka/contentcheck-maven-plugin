package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * Collects classpath and registers all resource conflicts during that
 *
 * @author Petr Kozelka
 */
class ClasspathResources {
    final ConflictCollector conflictCollector = new ConflictCollector();
    final Map<String, ResourceWithOptions> resourcesByUri = new HashMap<String, ResourceWithOptions>();

    public void addResource(ResourceInfo resource, ArchiveInfo archive) {
        final String resourceUri = resource.getUri();
        final String myHash = resource.getHash();
        ResourceWithOptions rwo = resourcesByUri.get(resourceUri);
        if (rwo == null) {
            rwo = new ResourceWithOptions();
            rwo.uri = resourceUri;
            resourcesByUri.put(resourceUri, rwo);
        } else {
            for (ArchiveInfo candidate : rwo.allCandidates) {
                final ResourceInfo hisResource = findResourceByUri(candidate, resourceUri);
                final String hisHash = hisResource.getHash();
                final boolean isDuplicate =  myHash.equals(hisHash);
                conflictCollector.addOverlap(candidate, archive, resource, isDuplicate);
                conflictCollector.addOverlap(archive, candidate, resource, isDuplicate);
            }
        }
        rwo.addCandidate(myHash, archive);
    }

    static ResourceInfo findResourceByUri(ArchiveInfo archive, String resourceUri) {
        for (ResourceInfo resource : archive.getResources()) {
            if (resource.getUri().equals(resourceUri)) {
                return resource;
            }
        }
        return null;
    }

    public Collection<? extends ConflictCheckResponse.ArchiveConflict> getConflicts() {
        return conflictCollector.getAll();
    }
}
