package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

/**
 * Collects classpath and registers all resource conflicts during that
 * @author Petr Kozelka
 */
class ClasspathResources {
    final ConflictCollector conflictCollector = new ConflictCollector();
    final Map<String, ResourceWithOptions> resourcesByUri = new HashMap<String, ResourceWithOptions>();

    public void addResource(ResourceInfo resource, ArchiveInfo archive) {
        final String resourceUri = resource.getUri();
        ResourceWithOptions rwo = resourcesByUri.get(resourceUri);
        if (rwo == null) {
            rwo = new ResourceWithOptions();
            rwo.uri = resourceUri;
            resourcesByUri.put(resourceUri, rwo);
        } else {
            for (ArchiveInfo candidate : rwo.allCandidates) {
                conflictCollector.addConflict(candidate, archive, resource);
                conflictCollector.addConflict(archive, candidate, resource);
            }
        }
        rwo.addCandidate(resource.getHash(), archive);
    }

    public Collection<? extends ConflictCheckResponse.ArchiveConflict> getConflicts() {
        return conflictCollector.getAll();
    }
}
