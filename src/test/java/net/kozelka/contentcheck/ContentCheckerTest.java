package net.kozelka.contentcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

public class ContentCheckerTest {

    @Test
    public void testUnexpectedEntries() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testUnexpectedEntriesInDirectory() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content.txt");
        File directoryToBeChecked = SupportUtils.getFile("test");
        CheckerOutput checkerOutput = checker.check(listingFile, directoryToBeChecked);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertTrue("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"));
        assertTrue("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"));
        assertThat("Incorrect number of unexpected entries", diffUnexpectedEntries.size(), is(2));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchives() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, true, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/a.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(false));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testCheckFilePatternScan() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "WEB-INF/**/*");
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
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
        ContentChecker checker = new ContentChecker(log, true, "com.buildbox", "Producer", SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/c.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(false));
    }

    @Test
    public void testMissingEntries() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content-missing-entries.txt");
        File archiveFile = SupportUtils.getFile("test.war");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffMissingEntries = checkerOutput.diffMissingEntries();
        assertThat("Missing entry WEB-INF/lib/d.jar in the collection of missing entries.", diffMissingEntries.contains("WEB-INF/lib/d.jar"), is(true));
    }
    
    @Test
    public void testReadListingFile() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content-read-listing-test.txt");
        Set<String> content = checker.readListing(listingFile);
        assertThat(content.size(), is(5));
    }
    
    @Test
    public void testReadListinFileWithDuplicitEntries() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content-duplicit-entries-test.txt");
        checker.readListing(listingFile);
        verify(log, times(1)).warn(anyString());
    }
    
    @Test
    public void testReadListingFileEmptyLines() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content-empty-lines-test.txt");
        Set<String> entries = checker.readListing(listingFile);
        assertThat("Unexpecting count of entries. Whitespaces and empty lines must be ignored.", entries.size(), is(0));
    }

    @Test
    public void testTopLevelJARS() throws IOException {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = SupportUtils.getFile("content-toplevel-jars.txt");
        File archiveFile = SupportUtils.getFile("test.ear");
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        assertThat("Default file matching pattern (All JARs) is broken, there are reported missing entries but shouldn't.", checkerOutput.diffMissingEntries().isEmpty(), is(true));
        assertThat("Default file matching pattern (All JARs) is broken, there are reported unexpected entries but shouldn't.",checkerOutput.diffUnexpectedEntries().isEmpty(), is(true));
    }
}