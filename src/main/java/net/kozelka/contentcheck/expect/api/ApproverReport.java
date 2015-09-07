package net.kozelka.contentcheck.expect.api;

import java.util.Set;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.expect.model.ApprovedEntry;

/**
 * This class represents a captured output from content check.
 */
public class ApproverReport {
    private final Set<ApprovedEntry> approvedEntries;
    private final Set<ActualEntry> actualEntries;
    private Set<ActualEntry> unexpectedEntries;
    private Set<ApprovedEntry> missingEntries;

    public ApproverReport(Set<ApprovedEntry> approvedEntries, Set<ActualEntry> archiveContent) {
        super();
        this.approvedEntries = approvedEntries;
        this.actualEntries = archiveContent;
    }

    public Set<ApprovedEntry> getApprovedEntries() {
        return approvedEntries;
    }

    public Set<ActualEntry> getActualEntries() {
        return actualEntries;
    }

    /**
     * @return the set of unexpected entries.
     */
    public Set<ActualEntry> getUnexpectedEntries() {
        return unexpectedEntries;
    }

    public void setUnexpectedEntries(Set<ActualEntry> unexpectedEntries) {
        this.unexpectedEntries = unexpectedEntries;
    }

    /**
     * @return the set of entries that should be in present in the source, but they are not.
     */
    public Set<ApprovedEntry> getMissingEntries() {
        return missingEntries;
    }

    public void setMissingEntries(Set<ApprovedEntry> missingEntries) {
        this.missingEntries = missingEntries;
    }
}
