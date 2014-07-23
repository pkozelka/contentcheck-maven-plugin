package net.kozelka.contentcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kozelka.contentcheck.introspection.ContentIntrospector;

/**
 * The content checker implementation. 
 */
public class ContentChecker {

    private ContentCheckerListener listener;
    private ContentIntrospector introspector;

    public ContentCheckerListener getListener() {
        return listener;
    }

    public void setListener(ContentCheckerListener listener) {
        this.listener = listener;
    }

    public ContentIntrospector getIntrospector() {
        return introspector;
    }

    public void setIntrospector(ContentIntrospector introspector) {
        this.introspector = introspector;
    }

    /**
     * Checks a content of {@code sourceFile} according to an allowed content defined by {@code listingFile}.
     * 
     * @param listingFile a file that defines allowed content
     * @param sourceFile directory or archive file to be checked
     * 
     * @return the result of source check
     * 
     * @throws IOException if something very bad happen
     */
    public CheckerOutput check(final File listingFile, final File sourceFile) throws IOException{
        final Set<String> allowedEntries = readListing(listingFile);
        final int count = introspector.readEntries(sourceFile);
        //XXX dagi: duplicit entries detection https://github.com/pkozelka/contentcheck-maven-plugin/issues#issue/4
        final Set<String> entries = introspector.getEntries();
        listener.summary(sourceFile, entries.size(), count);
        return new CheckerOutput(allowedEntries, entries);
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
                        listener.duplicate(listingFile, line);
                    }
                    expectedPaths.add(line);
                } 
            }
            listener.contentListingSummary(listingFile, expectedPaths.size(), totalCnt);
            return expectedPaths;
        } finally {
            reader.close();
        }
    }
}