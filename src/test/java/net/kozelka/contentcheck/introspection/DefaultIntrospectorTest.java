package net.kozelka.contentcheck.introspection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.kozelka.contentcheck.SupportUtils;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;


public class DefaultIntrospectorTest {

    @Test
    public void testIntrospection() throws IOException{
        Log log = mock(Log.class);
        DefaultIntrospector introspector = new DefaultIntrospector(log, false, SupportUtils.VENDOR1, SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File sourceArchive = SupportUtils.getFile("test.war");
        introspector.readEntries(sourceArchive);
        Set<String> sourceEntries = introspector.getEntries();
        assertThat("Unexpected count of source archive entries", sourceEntries.size(), is(3));
        assertThat("Missing entry WEB-INF/lib/a.jar in collection of source archive entries", sourceEntries.contains("WEB-INF/lib/a.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/b.jar in collection of source archive entries", sourceEntries.contains("WEB-INF/lib/b.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in collection of source archive entries", sourceEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }
    
    @Test
    public void testIntrospectionWithIgnoringOwnArtifacts() throws IOException{
        Log log = mock(Log.class);
        DefaultIntrospector introspector = new DefaultIntrospector(log, true, SupportUtils.VENDOR1, SupportUtils.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        File archive = SupportUtils.getFile("test.war");
        introspector.readEntries(archive);
        Set<String> sourceEntries = introspector.getEntries();
        assertThat("Unexpected count of source entries", sourceEntries.size(), is(2));
        assertThat("Missing entry WEB-INF/lib/b.jar in collection of source archive entries", sourceEntries.contains("WEB-INF/lib/b.jar"), is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in collection of source archive entries", sourceEntries.contains("WEB-INF/lib/c.jar"), is(true));
    }
}
