package net.kozelka.contentcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import net.kozelka.contentcheck.util.EventSink;

/**
 * The content checker implementation. 
 */
public class ContentChecker {

    private final EventSink<Events> events = EventSink.create(Events.class);
    private ContentIntrospector introspector;

    static boolean entrysetContainsUri(Set<CheckerEntry> entryset, String uri) {
        for (CheckerEntry checkerEntry : entryset) {
            if (checkerEntry.getUri().equals(uri)) return true;
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
     * Checks a content of {@code sourceFile} according to an allowed content defined by {@code listingFile}.
     * 
     * @param listingFile a file that defines allowed content
     *
     * @return the result of source check
     * 
     * @throws IOException if something very bad happen
     */
    public CheckerOutput check(final File listingFile) throws IOException{
        final Set<CheckerEntry> approvedEntries = readApprovedContent(listingFile);
        final Set<String> actualEntries = new LinkedHashSet<String>();
        final ContentIntrospector.Events collector = new ContentIntrospector.ContentCollector(actualEntries);
        introspector.getEvents().addListener(collector);
        final int totalCount = introspector.walk();
        introspector.getEvents().removeListener(collector);
        //XXX dagi: duplicit entries detection https://github.com/pkozelka/contentcheck-maven-plugin/issues#issue/4
        events.fire.summary(introspector.getSourceFile(), actualEntries.size(), totalCount);
        return compareEntries(approvedEntries, actualEntries);
    }

    static CheckerOutput compareEntries(Set<CheckerEntry> approvedEntries, Set<String> actualEntries) {
        final Set<String> unexpectedEntries = new LinkedHashSet<String>(actualEntries.size());
        for (String actual : actualEntries) {
            boolean found = false;
            for (CheckerEntry approved : approvedEntries) {
                if (approved.match(actual)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unexpectedEntries.add(actual);
            }
        }
        final CheckerOutput result = new CheckerOutput(approvedEntries, actualEntries);
        result.setUnexpectedEntries(unexpectedEntries);

        // TODO: merge these two iterations into one; remove from a working set while doing first iteration, etc. Beware of regexes on one side.

        final Set<CheckerEntry> missingEntries = new LinkedHashSet<CheckerEntry>(approvedEntries.size());
        for (CheckerEntry approved : approvedEntries) {
            boolean found = false;
            for (String actual : actualEntries) {
                if (approved.match(actual)) {
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

    protected Set<CheckerEntry> readApprovedContent(final File approvedContentFile) throws IOException {
        final Set<CheckerEntry> approvedContent = new LinkedHashSet<CheckerEntry>();
        final BufferedReader reader = new BufferedReader(new FileReader(approvedContentFile));
        try {
            int totalCnt = 0;
            String line;
            while ((line = reader.readLine())!= null) {
                totalCnt ++;
                line = line.trim();
                // we ignore empty and comments lines
                if (line.length() == 0) continue;
                if (line.startsWith("#")) continue;
                // TODO: this is a bit incorrect, because line is now rather "rule" that can have multiple, hard-to-compare forms. We should replace this with checking that each occurrence is matched by exactly one rule.
                if(entrysetContainsUri(approvedContent, line)) {
                    events.fire.duplicate(approvedContentFile, line);
                }
                final CheckerEntry entry = new CheckerEntry();
                entry.setUri(line);
                approvedContent.add(entry);
            }
            events.fire.contentListingSummary(approvedContentFile, approvedContent.size(), totalCnt);
            return approvedContent;
        } finally {
            reader.close();
        }
    }

    public static interface Events {
        void summary(File sourceFile, int checkedCount, int totalCount);

        void duplicate(File listingFile, String line);

        void contentListingSummary(File listingFile, int pathCount, int totalCount);
    }

}