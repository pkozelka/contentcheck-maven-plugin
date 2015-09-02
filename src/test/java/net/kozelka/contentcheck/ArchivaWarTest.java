package net.kozelka.contentcheck;

import java.io.File;

import java.util.List;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.impl.ClassConflictAnalyzer;
import net.kozelka.contentcheck.conflict.impl.ClassConflictPrinter;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.util.ArchiveLoader;
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
        final ClassConflictAnalyzer ccd = new ClassConflictAnalyzer();
        final List<ArchiveInfo> archives = ArchiveLoader.loadWar(archivaWar);
        final ClassConflictReport response = ccd.analyze(archives);
        final ClassConflictPrinter printer = new ClassConflictPrinter();
        printer.setPreviewThreshold(2);
        printer.setOutput(new DefaultConsumer());
        printer.print(response);
        Assert.assertEquals("Total overlaps", 290, response.getTotalOverlaps());
        Assert.assertEquals("Total entries", 235, response.getExploredArchives().size());
        Assert.assertEquals("Archive conflicts", 18, response.getArchiveConflicts().size());
    }
}
