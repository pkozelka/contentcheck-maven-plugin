package net.sf.buildbox.maven.contentcheck.introspection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.sf.buildbox.maven.contentcheck.ContentCheckMojo;
import net.sf.buildbox.maven.contentcheck.SupportUtils;
import net.sf.buildbox.maven.contentcheck.introspection.DefaultIntrospector;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;


public class DefaultIntrospectorTest {

    @Test
    public void testIntrospection() throws IOException{
        Log log = mock(Log.class);
        DefaultIntrospector introspector = new DefaultIntrospector(log, false, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File archive = SupportUtils.getFile("test.war");
        introspector.readArchive(archive);
        Set<String> archiveEntries = introspector.getArchiveEntries();
        assertThat("Unexpected count of archiveEntries", archiveEntries.size(), is(3));
        assertThat("Missing entry WEB-INF/lib/a.jar in collection of archiveEntries", archiveEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/b.jar in collection of archiveEntries", archiveEntries.contains("WEB-INF/lib/b.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in collection of archiveEntries", archiveEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }
    
    @Test
    public void testIntrospectionWithIgnoringOwnArtifacts() throws IOException{
        Log log = mock(Log.class);
        DefaultIntrospector introspector = new DefaultIntrospector(log, true, "com.buildbox", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, ContentCheckMojo.DEFAULT_CHECK_FILES_PATTERN);
        File archive = SupportUtils.getFile("test.war");
        introspector.readArchive(archive);
        Set<String> archiveEntries = introspector.getArchiveEntries();
        assertThat("Unexpected count of archiveEntries", archiveEntries.size(), is(2));
        assertThat("Missing entry WEB-INF/lib/b.jar in collection of archiveEntries", archiveEntries.contains("WEB-INF/lib/b.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in collection of archiveEntries", archiveEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }
}
