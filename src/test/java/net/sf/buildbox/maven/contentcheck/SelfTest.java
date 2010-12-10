package net.sf.buildbox.maven.contentcheck;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import net.sf.buildbox.maven.contentcheck.introspection.DefaultIntrospector;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests against zip generated from this project's own dependencies - see pom.xml (execution pack-deps-for-junit).
 * Serves both to keep deps under control, and to cover more cases.
 */
public class SelfTest {
    @Test
    public void testMyOwnDependencies() throws IOException {
        final Log log = mock(Log.class);
        final DefaultIntrospector introspector = new DefaultIntrospector(log, false, "zzz", ContentCheckMojo.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "**/*.jar");
        final File archive = SupportUtils.getFile("/myself.zip");
        introspector.readArchive(archive);
        final Set<String> archiveEntries = introspector.getArchiveEntries();
        assertThat("Missing toplevel entry junit-*.jar in collection of archiveEntries", archiveEntries.contains("junit-4.8.2.jar"), is(true));
        assertThat("Unexpected count of archiveEntries", archiveEntries.size(), is(18));
    }

}
