package net.sf.buildbox.maven.contentcheck;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represents a captured output from content check.
 */
public class CheckerOutput {
    private final Set<String> allowedEntries; 
    private final  Set<String> sourceEntries;

    public CheckerOutput(Set<String> allowedEntries, Set<String> archiveContent) {
        super();
        this.allowedEntries = allowedEntries;
        this.sourceEntries = archiveContent;
    }

    public Set<String> getAllowedEntries() {
        return allowedEntries;
    }

    public Set<String> getSourceEntries() {
        return sourceEntries;
    }

    /**
     * Diff allowed entries and entries present in the source.
     * @return the set of unexpected entries.
     */
    public Set<String> diffUnexpectedEntries() {
        final Set<String> unexpectedEntries = new LinkedHashSet<String>(sourceEntries);
        unexpectedEntries.removeAll(allowedEntries);
        return unexpectedEntries;
    }

    /**
     * Diff allowed entries and entries present in the source.
     * @return the set of entries that should be in present in the source, but they are not.
     */
    public Set<String> diffMissingEntries() {
        final Set<String> missingEntries = new LinkedHashSet<String>(allowedEntries);
        missingEntries.removeAll(sourceEntries);
        return missingEntries;
    }
}