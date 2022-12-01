package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractIntrospectorStrategyTest {

    private final IntrospectorInputStrategy directoryStrategy;
    private final File containerFileToBeChecked;

    public AbstractIntrospectorStrategyTest(IntrospectorInputStrategy directoryStrategy, File containerFileToBeChecked) {
        this.directoryStrategy = directoryStrategy;
        this.containerFileToBeChecked = containerFileToBeChecked;
    }


    @Test
    public void testReadAllEntries() throws Exception {
        final Collection<String> entries = directoryStrategy.list(containerFileToBeChecked);
        assertNotNull("entries cannot be null", entries);
        MatcherAssert.assertThat("Unexpected entries",
                entries,
                Matchers.containsInAnyOrder("WEB-INF/", "WEB-INF/lib/", "WEB-INF/lib/a.jar", "WEB-INF/lib/b.jar",
                                            "WEB-INF/lib/c.jar","WEB-INF/testfile.txt"));
    }

    @Test
    public void testReadEntryData() throws Exception {
        final InputStream data = directoryStrategy.getInputStream(containerFileToBeChecked, "WEB-INF/testfile.txt");
        assertNotNull("content of WEB-INF/testfile.txt should not be empty!", data);
        MatcherAssert.assertThat((String) IOUtils.readLines(data).get(0), is("Lorem ipsumLorem ipsumLorem ipsumLorem ipsum"));
    }
}
