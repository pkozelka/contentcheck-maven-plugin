package net.kozelka.contentcheck.expect.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.expect.model.ApprovedEntry;
import net.kozelka.contentcheck.expect.api.ApproverReport;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import net.kozelka.contentcheck.util.EventSink;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * The content checker implementation.
 */
public class ContentChecker {

    private final EventSink<Events> events = EventSink.create(Events.class);
    private ContentIntrospector introspector;

    static boolean entrysetContainsUri(Set<ApprovedEntry> entryset, String uri) {
        for (ApprovedEntry approvedEntry : entryset) {
            if (approvedEntry.getUri().equals(uri)) return true;
        }
        return false;
    }

    public EventSink<Events> getEvents() {
        return events;
    }

    public void setIntrospector(ContentIntrospector introspector) {
        this.introspector = introspector;
    }

    /**
     * Checks a content of {@code sourceFile} according to an allowed content defined by {@code approvedContentFile}.
     *
     * @param approvedContentFile a file that defines allowed content
     * @return the result of source check
     * @throws IOException if something very bad happen
     */
    public ApproverReport check(final File approvedContentFile) throws IOException{
        final Set<ApprovedEntry> approvedEntries = readApprovedContent(approvedContentFile);
        events.fire.contentListingSummary(approvedContentFile, approvedEntries.size());
        final Set<ActualEntry> actualEntries = new LinkedHashSet<ActualEntry>();
        final ContentIntrospector.Events collector = new ContentCollector(actualEntries);
        introspector.getEvents().addListener(collector);
        final int totalCount = introspector.walk();
        introspector.getEvents().removeListener(collector);
        //XXX dagi: duplicit entries detection https://github.com/pkozelka/contentcheck-maven-plugin/issues#issue/4
        events.fire.summary(introspector.getSourceFile(), actualEntries.size(), totalCount);
        return compareEntries(approvedEntries, actualEntries);
    }

    static boolean match(String approvedPattern, String actual) {
        return SelectorUtils.matchPath(approvedPattern, actual);
    }

    static ApproverReport compareEntries(Set<ApprovedEntry> approvedEntries, Set<ActualEntry> actualEntries) {
        final Set<ActualEntry> unexpectedEntries = new LinkedHashSet<ActualEntry>(actualEntries.size());
        for (ActualEntry actual : actualEntries) {
            boolean found = false;
            for (ApprovedEntry approved : approvedEntries) {
                if (match(approved.getUri(), actual.getUri())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unexpectedEntries.add(actual);
            }
        }
        final ApproverReport result = new ApproverReport(approvedEntries, actualEntries);
        result.setUnexpectedEntries(unexpectedEntries);

        // TODO: merge these two iterations into one; remove from a working set while doing first iteration, etc. Beware of regexes on one side.

        final Set<ApprovedEntry> missingEntries = new LinkedHashSet<ApprovedEntry>(approvedEntries.size());
        for (ApprovedEntry approved : approvedEntries) {
            boolean found = false;
            for (ActualEntry actual : actualEntries) {
                if (match(approved.getUri(), actual.getUri())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingEntries.add(approved);
            }
        }
        result.setMissingEntries(missingEntries);
        return result;
    }

    protected Set<ApprovedEntry> readApprovedContent(final File approvedContentFile) throws IOException {
        //TODO this should become a separate "parser/loader" class, to enable support of multiple formats and new features
        final Set<ApprovedEntry> approvedContent = new LinkedHashSet<ApprovedEntry>();
        final BufferedReader reader = new BufferedReader(new FileReader(approvedContentFile));
        try {
            String line;
            while ((line = reader.readLine())!= null) {
                line = line.trim();
                // we ignore empty and comments lines
                if (line.length() == 0) continue;
                if (line.startsWith("#")) continue;
                // TODO: this is a bit incorrect, because line is now rather "rule" that can have multiple, hard-to-compare forms. We should replace this with checking that each occurrence is matched by exactly one rule.
                if(entrysetContainsUri(approvedContent, line)) {
                    events.fire.duplicate(approvedContentFile, line);
                }
                final ApprovedEntry entry = new ApprovedEntry();
                entry.setUri(line);
                approvedContent.add(entry);
            }
            return approvedContent;
        } finally {
            reader.close();
        }
    }

    public interface Events {
        void summary(File sourceFile, int checkedCount, int totalCount);

        void duplicate(File listingFile, String line);

        void contentListingSummary(File listingFile, int pathCount);
    }

}
