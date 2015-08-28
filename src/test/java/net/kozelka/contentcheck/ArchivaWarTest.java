package net.kozelka.contentcheck;

import java.io.File;

import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.impl.ClassConflictDetector;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import net.kozelka.contentcheck.introspection.VendorFilter;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Petr Kozelka
 */
public class ArchivaWarTest {
    private File archivaWar;
    private File archivaApprovedContent;

    @Before
    public void setup() {
        archivaWar = SupportUtils.getFile("/archiva-webapp.war");
        archivaApprovedContent = SupportUtils.getFile("/archiva/approved-content.txt");
    }

    @Test
    public void contentCheck() throws Exception {

        final ContentChecker cc  = new ContentChecker();
        final ContentIntrospector introspector = new ContentIntrospector();
        introspector.setEntryContentFilter(new VendorFilter("org.apache.archiva"));
        introspector.setSourceFile(archivaWar);
        cc.setIntrospector(introspector);
        final CheckerOutput result = cc.check(archivaApprovedContent);
        //
        Assert.assertEquals("Missing entries", 5, result.getMissingEntries().size());
        Assert.assertEquals("Unexpected entries", 0, result.getUnexpectedEntries().size());
        Assert.assertEquals("Actual entries", 230, result.getActualEntries().size());
        Assert.assertEquals("Approved entries", 235, result.getApprovedEntries().size());
    }

    @Test
    public void classConflicts() throws Exception {
        final ClassConflictDetector ccd = new ClassConflictDetector();
        final ConflictCheckResponse response = ccd.exploreWar(archivaWar);
        final int totalConflicts = ccd.printResults(response, 2, new DefaultConsumer());
        Assert.assertEquals("Total conflicts", 290, totalConflicts);
        Assert.assertEquals("Total entries", 235, response.getExploredArchiveCount());
        Assert.assertEquals("Conflicting archives", 17, response.getConflictingArchives().size());
    }
}
