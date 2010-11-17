package net.sf.buildbox.maven.contentcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import java.util.Set;


import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.InOrder;

public class ContentCheckerTest {

    @Test
    public void testUnexpectedEntries() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = FileUtils.toFile(getClass().getResource("content.txt"));
        File archiveFile = FileUtils.toFile(getClass().getResource("test.war"));
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Missing entry WEB-INF/lib/a.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testUnexpectedEntriesIgnoreVendorArchives() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, true, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = FileUtils.toFile(getClass().getResource("content.txt"));
        File archiveFile = FileUtils.toFile(getClass().getResource("test.war"));
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/a.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/a.jar"), is(false));
        assertThat("Missing entry WEB-INF/lib/c.jar in the collection of unexpected entries.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }

    @Test
    public void testCheckFilePatternScan() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "WEB-INF/**/*");
        File listingFile = FileUtils.toFile(getClass().getResource("content.txt"));
        File archiveFile = FileUtils.toFile(getClass().getResource("test.war"));
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
        File listingFile = FileUtils.toFile(getClass().getResource("content.txt"));
        File archiveFile = FileUtils.toFile(getClass().getResource("test.war"));
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffUnexpectedEntries = checkerOutput.diffUnexpectedEntries();
        assertThat("Entry WEB-INF/lib/c.jar must not be in the collection of unexpected entries, because it's a vendor archive.", diffUnexpectedEntries.contains("WEB-INF/lib/c.jar"), is(false));
    }

    @Test
    public void testMissingEntries() throws Exception {
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = FileUtils.toFile(getClass().getResource("content-missing-entries.txt"));
        File archiveFile = FileUtils.toFile(getClass().getResource("test.war"));
        CheckerOutput checkerOutput = checker.check(listingFile, archiveFile);
        Set<String> diffMissingEntries = checkerOutput.diffMissingEntries();
        assertThat("Missing entry WEB-INF/lib/d.jar in the collection of missing entries.", diffMissingEntries.contains("WEB-INF/lib/d.jar"), is(true));
    }
    
    @Test
    public void testReadListingFile() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = FileUtils.toFile(getClass().getResource("content-read-listing-test.txt"));
        Set<String> content = checker.readListing(listingFile);
        assertThat(content.size(), is(5));
    }
    
    @Test
    public void testReadListinFileWithDuplicitEntries() throws Exception{
        Log log = mock(Log.class);
        ContentChecker checker = new ContentChecker(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File listingFile = FileUtils.toFile(getClass().getResource("content-duplicit-entries-test.txt"));
        checker.readListing(listingFile);
        verify(log, times(1)).warn(anyString());
    }
}