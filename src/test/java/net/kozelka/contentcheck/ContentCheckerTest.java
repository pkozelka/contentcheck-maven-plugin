package net.kozelka.contentcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.kozelka.contentcheck.introspection.ContentIntrospector;
import net.kozelka.contentcheck.introspection.VendorFilter;
import net.kozelka.contentcheck.mojo.MyContentCheckerListener;
import net.kozelka.contentcheck.mojo.MyIntrospectionListener;
import org.junit.Before;
import org.junit.Test;

public class ContentCheckerTest {
    private MyContentCheckerListener contentCheckerListener;
    private MyIntrospectionListener introspectionListener;

    @Before
    public void setup() {
        contentCheckerListener = mock(MyContentCheckerListener.class);
        introspectionListener = mock(MyIntrospectionListener.class);
    }

    @Test
    public void testUnexpectedEntries() throws Exception {
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, false,
                SupportUtils.VENDOR1,
                VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME,
                SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        CheckerOutput checkerOutput = checker.check(listingFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testUnexpectedEntriesInDirectory() throws Exception {
        File listingFile = SupportUtils.getFile("content.txt");
        File directoryToBeChecked = SupportUtils.getFile("test");
        final ContentChecker checker = createContentChecker(directoryToBeChecked, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        CheckerOutput checkerOutput = checker.check(listingFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertTrue("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"));
        assertTrue("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"));
        assertThat("Incorrect number of unexpected entries", diffUnexpectedEntries.size(), is(2));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchives() throws Exception {
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, true, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        CheckerOutput checkerOutput = checker.check(listingFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/a.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(false));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testCheckFilePatternScan() throws Exception {
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "WEB-INF/**/*");
        CheckerOutput checkerOutput = checker.check(listingFile);
        Set<String> diffMissingEntries = checkerOutput.diffMissingEntries();
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();

        assertThat("Entry WEB-INF/testfile.txt is reported as missing but should not.", diffMissingEntries.contains("WEB-INF/testfile.txt"), is(false));
        assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchivesCustomVendorHeader() throws Exception {
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, true, SupportUtils.VENDOR1, "Producer", SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        CheckerOutput checkerOutput = checker.check(listingFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/c.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(false));
    }

    @Test
    public void testMissingEntries() throws Exception {
        File listingFile = SupportUtils.getFile("content-missing-entries.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        CheckerOutput checkerOutput = checker.check(listingFile);
        Set<String> diffMissingEntries = checkerOutput.diffMissingEntries();
        assertThat("Missing entry WEB-INF/lib/d.jar in the collection of missing entries.", diffMissingEntries.contains("WEB-INF/lib/d.jar"), is(true));
    }

    @Test
    public void testReadListingFile() throws Exception{
        final ContentChecker checker = new ContentChecker();
        File listingFile = SupportUtils.getFile("content-read-listing-test.txt");
        Set<String> content = checker.readListing(listingFile);
        assertThat(content.size(), is(5));
    }

    @Test
    public void testReadListinFileWithDuplicitEntries() throws Exception{
        final ContentChecker checker = new ContentChecker();
        checker.getEvents().addListener(contentCheckerListener);
        File listingFile = SupportUtils.getFile("content-duplicit-entries-test.txt");
        checker.readListing(listingFile);
        verify(contentCheckerListener, times(1)).duplicate(any(File.class), anyString());
    }

    @Test
    public void testReadListingFileEmptyLines() throws Exception{
        final ContentChecker checker = new ContentChecker();
        File listingFile = SupportUtils.getFile("content-empty-lines-test.txt");
        Set<String> entries = checker.readListing(listingFile);
        assertThat("Unexpecting count of entries. Whitespaces and empty lines must be ignored.", entries.size(), is(0));
    }

    @Test
    public void testTopLevelJARS() throws IOException {
        File listingFile = SupportUtils.getFile("content-toplevel-jars.txt");
        File archiveFile = SupportUtils.getFile("test.ear");
        final ContentChecker checker = createContentChecker(archiveFile, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        CheckerOutput checkerOutput = checker.check(listingFile);
        assertThat("Default file matching pattern (All JARs) is broken, there are reported missing entries but shouldn't.", checkerOutput.diffMissingEntries().isEmpty(), is(true));
        assertThat("Default file matching pattern (All JARs) is broken, there are reported unexpected entries but shouldn't.",checkerOutput.diffUnexpectedEntries().isEmpty(), is(true));
    }

    private ContentChecker createContentChecker(File sourceFile, boolean ignoreVendorArchives, String vendor, String vendorManifestEntryName, String checkFilesPattern) {

        final ContentChecker contentChecker = new ContentChecker();
        contentChecker.getEvents().addListener(contentCheckerListener);
        final ContentIntrospector introspector = ContentIntrospector.create(introspectionListener,
                ignoreVendorArchives,
                vendor,
                vendorManifestEntryName,
                checkFilesPattern);
        introspector.setSourceFile(sourceFile);
        contentChecker.setIntrospector(introspector);
        return contentChecker;
    }

}