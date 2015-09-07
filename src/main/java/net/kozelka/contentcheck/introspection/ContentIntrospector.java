package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import net.kozelka.contentcheck.util.EventSink;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * This introspector captures all passed entries by their paths.
 */
public class ContentIntrospector {
    public static final FilenameFilter ISJAR_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };
    private EventSink<Events> events = EventSink.create(Events.class);
    private FilenameFilter entryNameFilter = ISJAR_FILTER;
    private EntryContentFilter entryContentFilter;
    private File sourceFile;
    private IntrospectorInputStrategy walker;

    public static ContentIntrospector create(Events listener, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
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

    public EventSink<Events> getEvents() {
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

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
        if (sourceFile.isDirectory()) {
            walker = new DirectoryIntrospectorStrategy();
        } else {
            walker = new ZipArchiveIntrospectorStrategy();
        }
    }

    /**
     * Walks through the content of {@code sourceFile} entry by entry. If an entry passes {@link #setEntryNameFilter entryNameFilter}
     * and is not a vendor archive (in case we care)
     * the entry will be delegated to  {@link net.kozelka.contentcheck.introspection.ContentIntrospector.Events#processEntry(String)}
     * for further processing.
     *
     * @return the total number of processed entries, including skipped ones.
     */
    public final int walk() throws IOException {
        events.fire.readingSourceFile(sourceFile);
        int totalCnt = 0;
        for (String entryName : walker.list(sourceFile)) {
            totalCnt++;

            // filter by entry name
            if (!entryNameFilter.accept(sourceFile, entryName)) {
                events.fire.skippingEntryNotMatching(entryName);
                continue;
            }

            // filter by entry content
            if(entryContentFilter != null) {
                final InputStream entryContentStream = walker.getInputStream(sourceFile, entryName);
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
            events.fire.processEntry(entryName);
        }

        return totalCnt;
    }

    public interface EntryContentFilter {
        /**
         * Decides if given entry can be accepted, based on its name and content.
         * @param entryName -
         * @param entryContentStream  the content stream; caller will handle both opening and closing it
         * @return false if the entry should be skipped
         * @throws IOException when content processing has troubles
         */
        boolean accept(String entryName, InputStream entryContentStream) throws IOException;
    }

    public interface Events {
        void readingSourceFile(File sourceFile);

        void skippingEntryNotMatching(String entry);

        void skippingEntryOwnModule(String entry);

        void cannotCheckManifest(String jarPath, Exception e);

        void cannotClose(String jarPath, IOException e);

        void checkingInTmpfile(String jarPath, File tempFile);

        void processEntry(String entryName);
    }

}
