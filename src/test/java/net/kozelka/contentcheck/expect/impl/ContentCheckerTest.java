package net.kozelka.contentcheck.expect.impl;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import net.kozelka.contentcheck.SupportUtils;
import net.kozelka.contentcheck.expect.api.ApproverReport;
import net.kozelka.contentcheck.expect.model.ApprovedEntry;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import net.kozelka.contentcheck.introspection.VendorFilter;
import net.kozelka.contentcheck.mojo.MyContentCheckerListener;
import net.kozelka.contentcheck.mojo.MyIntrospectionListener;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class ContentCheckerTest {
    private MyContentCheckerListener contentCheckerListener;
    private MyIntrospectionListener introspectionListener;

    @Before
    public void setup() {
        contentCheckerListener = Mockito.mock(MyContentCheckerListener.class);
        introspectionListener = Mockito.mock(MyIntrospectionListener.class);
    }

    @Test
    public void testUnexpectedEntries() throws Exception {
        final File listingFile = SupportUtils.getFile("content.txt");
        final File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, false,
                SupportUtils.VENDOR1,
                VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME,
                SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final ApproverReport approverReport = checker.check(listingFile);
        final Set<String> diffUnexpectedEntries = approverReport.getUnexpectedEntries();
        Assert.assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), CoreMatchers.is(true));
        Assert.assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), CoreMatchers.is(true));
    }

    @Test
    public void testUnexpectedEntriesInDirectory() throws Exception {
        final File listingFile = SupportUtils.getFile("content.txt");
        final File directoryToBeChecked = SupportUtils.getFile("test");
        final ContentChecker checker = createContentChecker(directoryToBeChecked, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final ApproverReport approverReport = checker.check(listingFile);
        final Set<String> diffUnexpectedEntries = approverReport.getUnexpectedEntries();
        Assert.assertTrue("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"));
        Assert.assertTrue("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"));
        Assert.assertThat("Incorrect number of unexpected entries", diffUnexpectedEntries.size(), CoreMatchers.is(2));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchives() throws Exception {
        final File listingFile = SupportUtils.getFile("content.txt");
        final File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, true, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final ApproverReport approverReport = checker.check(listingFile);
        final Set<String> diffUnexpectedEntries = approverReport.getUnexpectedEntries();
        Assert.assertThat("Entry WEB-INF/lib/a.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), CoreMatchers.is(false));
        Assert.assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), CoreMatchers.is(true));
    }

    @Test
    public void testCheckFilePatternScan() throws Exception {
        final File listingFile = SupportUtils.getFile("content.txt");
        final File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "WEB-INF/**/*");
        final ApproverReport approverReport = checker.check(listingFile);
        final Set<ApprovedEntry> diffMissingEntries = approverReport.getMissingEntries();
        final Set<String> diffUnexpectedEntries = approverReport.getUnexpectedEntries();

        Assert.assertThat("Entry WEB-INF/testfile.txt is reported as missing but should not.", diffMissingEntries.contains("WEB-INF/testfile.txt"), CoreMatchers.is(false));
        Assert.assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), CoreMatchers.is(true));
        Assert.assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), CoreMatchers.is(true));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchivesCustomVendorHeader() throws Exception {
        final File listingFile = SupportUtils.getFile("content.txt");
        final File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, true, SupportUtils.VENDOR1, "Producer", SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final ApproverReport approverReport = checker.check(listingFile);
        final Set<String> diffUnexpectedEntries = approverReport.getUnexpectedEntries();
        Assert.assertThat("Entry WEB-INF/lib/c.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), CoreMatchers.is(false));
    }

    @Test
    public void testMissingEntries() throws Exception {
        final File listingFile = SupportUtils.getFile("content-missing-entries.txt");
        final File archiveFile = SupportUtils.getFile("test.war");
        final ContentChecker checker = createContentChecker(archiveFile, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final ApproverReport approverReport = checker.check(listingFile);
        final Set<ApprovedEntry> diffMissingEntries = approverReport.getMissingEntries();
        Assert.assertThat("Missing entry WEB-INF/lib/d.jar in the collection of missing entries.", ContentChecker.entrysetContainsUri(diffMissingEntries, "WEB-INF/lib/d.jar"), CoreMatchers.is(true));
    }

    @Test
    public void testReadListingFile() throws Exception{
        final ContentChecker checker = new ContentChecker();
        final File listingFile = SupportUtils.getFile("content-read-listing-test.txt");
        final Set<ApprovedEntry> content = checker.readApprovedContent(listingFile);
        Assert.assertThat(content.size(), CoreMatchers.is(5));
    }

    @Test
    public void testReadListinFileWithDuplicitEntries() throws Exception{
        final ContentChecker checker = new ContentChecker();
        checker.getEvents().addListener(contentCheckerListener);
        final File listingFile = SupportUtils.getFile("content-duplicit-entries-test.txt");
        checker.readApprovedContent(listingFile);
        Mockito.verify(contentCheckerListener, Mockito.times(1)).duplicate(Matchers.any(File.class), Matchers.anyString());
    }

    @Test
    public void testReadListingFileEmptyLines() throws Exception{
        final ContentChecker checker = new ContentChecker();
        final File listingFile = SupportUtils.getFile("content-empty-lines-test.txt");
        final Set<ApprovedEntry> entries = checker.readApprovedContent(listingFile);
        Assert.assertThat("Unexpecting count of entries. Whitespaces and empty lines must be ignored.", entries.size(), CoreMatchers.is(0));
    }

    @Test
    public void testTopLevelJARS() throws IOException {
        final File listingFile = SupportUtils.getFile("content-toplevel-jars.txt");
        final File archiveFile = SupportUtils.getFile("test.ear");
        final ContentChecker checker = createContentChecker(archiveFile, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final ApproverReport approverReport = checker.check(listingFile);
        Assert.assertThat("Default file matching pattern (All JARs) is broken, there are reported missing entries but shouldn't.", approverReport.getMissingEntries().isEmpty(), CoreMatchers.is(true));
        Assert.assertThat("Default file matching pattern (All JARs) is broken, there are reported unexpected entries but shouldn't.", approverReport.getUnexpectedEntries().isEmpty(), CoreMatchers.is(true));
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
