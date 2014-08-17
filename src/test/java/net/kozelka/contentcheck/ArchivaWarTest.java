package net.kozelka.contentcheck;

import java.io.File;
import net.kozelka.contentcheck.conflict.ClassConflictDetector;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import net.kozelka.contentcheck.introspection.VendorFilter;
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
        Assert.assertEquals("Missing entries", 5, result.diffMissingEntries().size());
        Assert.assertEquals("Unexpected entries", 0, result.diffUnexpectedEntries().size());
        Assert.assertEquals("Actual entries", 230, result.getActualEntries().size());
        Assert.assertEquals("Approved entries", 235, result.getApprovedEntries().size());
    }

    @Test
    public void classConflicts() throws Exception {
        final ClassConflictDetector ccd = new ClassConflictDetector();
        ccd.exploreWar(archivaWar);
        Assert.assertEquals("Total entries", 235, ccd.getExploredArchives().size());
        Assert.assertEquals("Conflicting archives", 17, ccd.getConflictingArchives().size());
        final int totalConflicts = ccd.printResults(2, new ClassConflictDetector.LineOutput() {
            @Override
            public void println(String line) {
                System.out.println(line);
            }
        });
        Assert.assertEquals("Total conflicts", 290, totalConflicts);
    }
}
