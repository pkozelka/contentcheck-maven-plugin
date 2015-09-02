package net.kozelka.contentcheck.conflict.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ArchiveConflict;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import net.kozelka.contentcheck.conflict.util.ArchiveLoader;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Detects class conflicts inside given set of classpath elements.
 * @author Petr Kozelka
 */
public class ClassConflictAnalyzer {

    public ClassConflictReport analyze(Collection<ArchiveInfo> archives) {
        final ClasspathResources cpr = new ClasspathResources();
        for (ArchiveInfo archive : archives) {
            for (ResourceInfo resource : archive.getResources()) {
                final String resourceName = resource.getUri();
                if (!resourceName.endsWith(".class")) continue;

                cpr.addResource(resource, archive);
            }
        }
        // prepare report
        final ClassConflictReport report = new ClassConflictReport();
        report.getExploredArchives().addAll(archives);
        report.getArchiveConflicts().addAll(cpr.getConflicts());
        report.getResources().addAll(cpr.getResources());
        // count: involved jars, class overlaps (duplications, conflicts),
        int totalOverlaps = 0;
        for (ArchiveConflict archiveConflict : cpr.getConflicts()) {
            totalOverlaps += archiveConflict.getOverlapingResources().size();
        }
        report.setTotalOverlaps(totalOverlaps);
        return report;
    }

    //TODO: move this to cli
    public static void main(String[] args) throws IOException {
        final int previewThreshold = 5;
        final File war = new File(args[0]);

        System.out.println("Detecting conflict in " + war);
        System.out.println("Class preview threshold: " + previewThreshold);
        final ClassConflictAnalyzer analyzer = new ClassConflictAnalyzer();
        final List<ArchiveInfo> archives = ArchiveLoader.loadWar(war);
        final ClassConflictReport report = analyzer.analyze(archives);
        final ClassConflictPrinter printer = new ClassConflictPrinter();
        printer.setPreviewThreshold(previewThreshold);
        printer.setOutput(new StreamConsumer() {
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
        printer.print(report);
    }
}
