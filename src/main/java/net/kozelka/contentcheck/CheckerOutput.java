package net.kozelka.contentcheck;

import org.codehaus.plexus.util.SelectorUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represents a captured output from content check.
 */
public class CheckerOutput {
    private final Set<String> approvedEntries;
    private final  Set<String> actualEntries;

    public CheckerOutput(Set<String> approvedEntries, Set<String> archiveContent) {
        super();
        this.approvedEntries = approvedEntries;
        this.actualEntries = archiveContent;
    }

    public Set<String> getApprovedEntries() {
        return approvedEntries;
    }

    public Set<String> getActualEntries() {
        return actualEntries;
    }

    /**
     * Diff allowed entries and entries present in the source.
     * @return the set of unexpected entries.
     */
    public Set<String> diffUnexpectedEntries() {
        final Set<String> unexpectedEntries = new LinkedHashSet<String>(actualEntries.size());
        for (String entry: actualEntries) {
            boolean found = false;
            for (String pattern: approvedEntries) {
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
        final Set<String> missingEntries = new LinkedHashSet<String>(approvedEntries.size());
        for (String pattern: approvedEntries) {
            boolean found = false;
            for (String entry: actualEntries) {
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