package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kozelka.contentcheck.util.EventSink;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * This introspector captures all passed entities by their paths.
 *
 * @see #sourceEntries
 */
public class ContentIntrospector {
    public static final FilenameFilter ISJAR_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };
    private final Set<String> sourceEntries = new LinkedHashSet<String>();
    private EventSink<IntrospectionListener> events = EventSink.create(IntrospectionListener.class);
    private FilenameFilter entryNameFilter = ISJAR_FILTER;
    private EntryContentFilter entryContentFilter;

    public static ContentIntrospector create(IntrospectionListener listener, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        final ContentIntrospector contentIntrospector = new ContentIntrospector();
        contentIntrospector.getEvents().addListener(listener);
        contentIntrospector.setCheckFilesPattern(checkFilesPattern);
        if (ignoreVendorArchives) {
            final VendorFilter vendorFilter = new VendorFilter(vendorId);
            vendorFilter.setManifestVendorEntry(manifestVendorEntry);
            vendorFilter.getEvents().addListener(listener);
            contentIntrospector.setEntryContentFilter(vendorFilter);
        }
        return contentIntrospector;
    }

    public EventSink<IntrospectionListener> getEvents() {
        return events;
    }

    public void setCheckFilesPattern(final String checkFilesPattern) {
        setEntryNameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return SelectorUtils.matchPath("/" + checkFilesPattern, "/" + name);
            }
        });
    }

    public void setEntryNameFilter(FilenameFilter entryNameFilter) {
        this.entryNameFilter = entryNameFilter;
    }

    public void setEntryContentFilter(EntryContentFilter entryContentFilter) {
        this.entryContentFilter = entryContentFilter;
    }

    private void processEntry(String entry) throws IOException {
        sourceEntries.add(entry);
    }

    /**
     * @return the entries found in source
     */
    public Set<String> getEntries() {
        return sourceEntries;
    }

    /**
     * Starts reading {@code sourceFile}'s content entry by entry. If an entry passes {@link #setEntryNameFilter entryNameFilter}
     * and is not a vendor archive (in case we care)
     * the entry will be delegated to the method {@link #processEntry(String)}
     * for further processing.
     *
     * @param sourceFile a source file to be read, typically an archive or directory
     *
     * @return the total number of processed entries, including skipped ones.
     *
     * @see #processEntry(String)
     */
    public final int readEntries(final File sourceFile) throws IOException {
        events.fire.readingSourceFile(sourceFile);
        final IntrospectorInputStrategy inputStrategy;
        if (sourceFile.isDirectory()) {
            inputStrategy = new DirectoryIntrospectorStrategy();
        } else {
            inputStrategy = new ZipArchiveIntrospectorStrategy();
        }

        int totalCnt = 0;
        for (String entryName : inputStrategy.list(sourceFile)) {
            totalCnt++;

            // filter by entry name
            if (!entryNameFilter.accept(sourceFile, entryName)) {
                events.fire.skippingEntryNotMatching(entryName);
                continue;
            }

            // filter by entry content
            if(entryContentFilter != null) {
                final InputStream entryContentStream = inputStrategy.getInputStream(sourceFile, entryName);
                try {
                    if(!entryContentFilter.accept(entryName, entryContentStream)) {
                        events.fire.skippingEntryOwnModule(entryName);
                        continue;
                    }
                } finally {
                    entryContentStream.close();
                }
            }
            //
            processEntry(entryName);
        }

        return totalCnt;
    }

    public static interface EntryContentFilter {
        /**
         * Decides if given entry can be accepted, based on its name and content.
         * @param entryName -
         * @param entryContentStream  the content stream; caller will handle both opening and closing it
         * @return false if the entry should be skipped
         * @throws IOException when content processing has troubles
         */
        boolean accept(String entryName, InputStream entryContentStream) throws IOException;
    }

    public static interface IntrospectionListener {
        void readingSourceFile(File sourceFile);

        void skippingEntryNotMatching(String entry);

        void skippingEntryOwnModule(String entry);

        void cannotCheckManifest(String jarPath, Exception e);

        void cannotClose(String jarPath, IOException e);

        void checkingInTmpfile(String jarPath, File tempFile);
    }
}
