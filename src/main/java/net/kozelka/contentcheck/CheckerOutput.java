package net.kozelka.contentcheck;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represents a captured output from content check.
 */
public class CheckerOutput {
    private final Set<CheckerEntry> approvedEntries;
    private final Set<String> actualEntries;

    public CheckerOutput(Set<CheckerEntry> approvedEntries, Set<String> archiveContent) {
        super();
        this.approvedEntries = approvedEntries;
        this.actualEntries = archiveContent;
    }

    public Set<CheckerEntry> getApprovedEntries() {
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
        for (String actual: actualEntries) {
            boolean found = false;
            for (CheckerEntry approved: approvedEntries) {
                if (approved.match(actual)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unexpectedEntries.add(actual);
            }
        }
        return unexpectedEntries;
    }

    /**
     * Diff allowed entries and entries present in the source.
     * @return the set of entries that should be in present in the source, but they are not.
     */
    public Set<CheckerEntry> diffMissingEntries() {
        final Set<CheckerEntry> missingEntries = new LinkedHashSet<CheckerEntry>(approvedEntries.size());
        for (CheckerEntry approved: approvedEntries) {
            boolean found = false;
            for (String actual: actualEntries) {
                if (approved.match(actual)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingEntries.add(approved);
            }
        }
        return missingEntries;
    }
}