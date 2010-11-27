package net.sf.buildbox.maven.contentcheck;

import static net.sf.buildbox.maven.contentcheck.SupportUtils.getFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

public class ContentCheckerTest {

    @Test
    public void testUnexpectedEntries() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content.txt");
        File archiveFile = getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchives() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, true, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content.txt");
        File archiveFile = getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/a.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(false));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testCheckFilePatternScan() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "WEB-INF/**/*");
        File listingFile = getFile("content.txt");
        File archiveFile = getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffMissingEntries = checkerOutput.diffMissingEntries();
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();

        assertThat("Entry WEB-INF/testfile.txt is reported as missing but should not.", diffMissingEntries.contains("WEB-INF/testfile.txt"), is(false));
        assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchivesCustomVendorHeader() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, true, "com.buildbox", "Producer", ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content.txt");
        File archiveFile = getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/c.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(false));
    }

    @Test
    public void testMissingEntries() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content-missing-entries.txt");
        File archiveFile = getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffMissingEntries = checkerOutput.diffMissingEntries();
        assertThat("Missing entry WEB-INF/lib/d.jar in the collection of missing entries.", diffMissingEntries.contains("WEB-INF/lib/d.jar"), is(true));
    }
    
    @Test
    public void testReadListingFile() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content-read-listing-test.txt");
        Set<String> content = checker.readListing(listingFile);
        assertThat(content.size(), is(5));
    }
    
    @Test
    public void testReadListinFileWithDuplicitEntries() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content-duplicit-entries-test.txt");
        checker.readListing(listingFile);
        verify(log, times(1)).warn(anyString());
    }
    
    @Test
    public void testReadListingFileEmptyLines() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = getFile("content-empty-lines-test.txt");
        Set<String> entries = checker.readListing(listingFile);
        assertThat("Unexpecting count of entries. Whitespaces and empty lines must be ignored.", entries.size(), is(0));
    }
}