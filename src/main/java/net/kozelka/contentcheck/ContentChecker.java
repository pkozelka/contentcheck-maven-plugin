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
        final Set<String> approvedEntries = readListing(listingFile);
        final int totalCount = introspector.readEntries();
        //XXX dagi: duplicit entries detection https://github.com/pkozelka/contentcheck-maven-plugin/issues#issue/4
        final Set<String> actualEntries = introspector.getEntries();
        events.fire.summary(introspector.getSourceFile(), actualEntries.size(), totalCount);
        return new CheckerOutput(approvedEntries, actualEntries);
    }

    protected Set<String> readListing(final File listingFile) throws IOException {
        final Set<String> expectedPaths = new LinkedHashSet<String>();
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
                    expectedPaths.add(line);
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