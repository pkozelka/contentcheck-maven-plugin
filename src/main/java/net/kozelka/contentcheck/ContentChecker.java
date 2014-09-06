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
        final Set<CheckerEntry> approvedEntries = readListing(listingFile);
        final Set<String> actualEntries = new LinkedHashSet<String>();
        final ContentIntrospector.Events collector = new ContentIntrospector.ContentCollector(actualEntries);
        introspector.getEvents().addListener(collector);
        final int totalCount = introspector.walk();
        introspector.getEvents().removeListener(collector);
        //XXX dagi: duplicit entries detection https://github.com/pkozelka/contentcheck-maven-plugin/issues#issue/4
        events.fire.summary(introspector.getSourceFile(), actualEntries.size(), totalCount);
        return new CheckerOutput(approvedEntries, actualEntries);
    }

    protected Set<CheckerEntry> readListing(final File listingFile) throws IOException {
        final Set<CheckerEntry> expectedPaths = new LinkedHashSet<CheckerEntry>();
        final BufferedReader reader = new BufferedReader(new FileReader(listingFile));
        try {
            int totalCnt = 0;
            String line;
            while ((line = reader.readLine())!= null) {
                totalCnt ++;
                line = line.trim();
                final boolean ignoreLine = line.length() == 0 || line.startsWith("#");// we ignore empty and comments lines
                if (!ignoreLine) { 
                    if(expectedPaths.contains(line)) {
                        events.fire.duplicate(listingFile, line);
                    }
                    final CheckerEntry entry = new CheckerEntry();
                    entry.setUri(line);
                    expectedPaths.add(entry);
                } 
            }
            events.fire.contentListingSummary(listingFile, expectedPaths.size(), totalCnt);
            return expectedPaths;
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