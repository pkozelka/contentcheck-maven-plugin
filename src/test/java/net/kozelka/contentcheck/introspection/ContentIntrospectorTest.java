package net.kozelka.contentcheck.introspection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.SupportUtils;
import net.kozelka.contentcheck.expect.TestUtils;
import net.kozelka.contentcheck.expect.impl.ContentCollector;
import net.kozelka.contentcheck.expect.impl.VendorFilter;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;


public class ContentIntrospectorTest {

    @Test
    public void testIntrospection() throws IOException{
        final ContentIntrospector.Events listener = mock(ContentIntrospector.Events.class);
        final ContentIntrospector introspector = ContentIntrospector.create(listener, false, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        final List<ActualEntry> sourceEntries = new ArrayList<ActualEntry>();
        final ContentIntrospector.Events collector = new ContentCollector(sourceEntries);
        introspector.getEvents().addListener(collector);
        introspector.setSourceFile(SupportUtils.getFile("test.war"));
        introspector.walk();
        assertThat("Unexpected count of source archive entries",
            sourceEntries.size(),
            is(3));
        assertThat("Missing entry WEB-INF/lib/a.jar in collection of source archive entries",
            TestUtils.contains(sourceEntries, "WEB-INF/lib/a.jar"),
            is(true));
        assertThat("Missing entry WEB-INF/lib/b.jar in collection of source archive entries",
            TestUtils.contains(sourceEntries, "WEB-INF/lib/b.jar"),
            is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in collection of source archive entries",
            TestUtils.contains(sourceEntries,
                "WEB-INF/lib/c.jar"),
            is(true));
    }

    @Test
    public void testIntrospectionWithIgnoringOwnArtifacts() throws IOException{
        final ContentIntrospector.Events listener = mock(ContentIntrospector.Events.class);
        final ContentIntrospector introspector = ContentIntrospector.create(listener, true, SupportUtils.VENDOR1, VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, SupportUtils.DEFAULT_CHECK_FILES_PATTERN);
        introspector.setSourceFile(SupportUtils.getFile("test.war"));
        final List<ActualEntry> sourceEntries = new ArrayList<ActualEntry>();
        final ContentIntrospector.Events collector = new ContentCollector(sourceEntries);
        introspector.getEvents().addListener(collector);
        introspector.walk();
        assertThat("Unexpected count of source entries", sourceEntries.size(), is(2));
        assertThat("Missing entry WEB-INF/lib/b.jar in collection of source archive entries",
            TestUtils.contains(sourceEntries, "WEB-INF/lib/b.jar"),
            is(true));
        assertThat("Missing entry WEB-INF/lib/c.jar in collection of source archive entries",
            TestUtils.contains(sourceEntries, "WEB-INF/lib/c.jar"),
            is(true));
    }
}
