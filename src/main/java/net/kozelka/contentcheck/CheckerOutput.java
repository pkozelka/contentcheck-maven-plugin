package net.kozelka.contentcheck;

import java.util.Set;

/**
 * This class represents a captured output from content check.
 */
public class CheckerOutput {
    private final Set<CheckerEntry> approvedEntries;
    private final Set<String> actualEntries;
    private Set<String> unexpectedEntries;
    private Set<CheckerEntry> missingEntries;

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
     * @return the set of unexpected entries.
     */
    public Set<String> getUnexpectedEntries() {
        return unexpectedEntries;
    }

    public void setUnexpectedEntries(Set<String> unexpectedEntries) {
        this.unexpectedEntries = unexpectedEntries;
    }

    /**
     * @return the set of entries that should be in present in the source, but they are not.
     */
    public Set<CheckerEntry> getMissingEntries() {
        return missingEntries;
    }

    public void setMissingEntries(Set<CheckerEntry> missingEntries) {
        this.missingEntries = missingEntries;
    }
}