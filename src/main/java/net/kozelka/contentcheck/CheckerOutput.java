package net.kozelka.contentcheck;

import org.codehaus.plexus.util.SelectorUtils;

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
        final Set<String> unexpectedEntries = new LinkedHashSet<String>(sourceEntries.size());
        for (String entry: sourceEntries) {
            boolean found = false;
            for (String pattern: allowedEntries) {
                if (SelectorUtils.matchPath(pattern, entry)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unexpectedEntries.add(entry);
            }
        }
        return unexpectedEntries;
    }

    /**
     * Diff allowed entries and entries present in the source.
     * @return the set of entries that should be in present in the source, but they are not.
     */
    public Set<String> diffMissingEntries() {
        final Set<String> missingEntries = new LinkedHashSet<String>(allowedEntries.size());
        for (String pattern: allowedEntries) {
            boolean found = false;
            for (String entry: sourceEntries) {
                if (SelectorUtils.matchPath(pattern, entry)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingEntries.add(pattern);
            }
        }
        return missingEntries;
    }
}