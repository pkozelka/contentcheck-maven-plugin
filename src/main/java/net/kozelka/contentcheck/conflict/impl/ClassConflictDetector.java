package net.kozelka.contentcheck.conflict.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import net.kozelka.contentcheck.conflict.util.ArchiveLoader;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Detects class conflicts inside given set of classpath elements.
 * @author Petr Kozelka
 */
public class ClassConflictDetector {

    public ConflictCheckResponse findConflicts(Collection<ArchiveInfo> archives) {
        final ClasspathResources cpr = new ClasspathResources();
        for (ArchiveInfo archive : archives) {
            for (ResourceInfo resource : archive.getResources()) {
                final String resourceName = resource.getUri();
                if (!resourceName.endsWith(".class")) continue;

                cpr.addResource(resource, archive);
            }
        }
        // prepare response
        final ConflictCheckResponse response = new ConflictCheckResponse();
        response.getExploredArchives().addAll(archives);
        response.getArchiveConflicts().addAll(cpr.getConflicts());
        response.getResources().addAll(cpr.getResources());
        // count: involved jars, class overlaps (duplications, conflicts),
        int totalOverlaps = 0;
        for (ConflictCheckResponse.ArchiveConflict archiveConflict : cpr.getConflicts()) {
            totalOverlaps += archiveConflict.getOverlapingResources().size();
        }
        response.setTotalOverlaps(totalOverlaps);
        return response;
    }

    //TODO: move this to cli
    public static void main(String[] args) throws IOException {
        final int previewThreshold = 5;
        final File war = new File(args[0]);

        System.out.println("Detecting conflict in " + war);
        System.out.println("Class preview threshold: " + previewThreshold);
        final ClassConflictDetector ccd = new ClassConflictDetector();
        final List<ArchiveInfo> archives = ArchiveLoader.loadWar(war);
        final ConflictCheckResponse response = ccd.findConflicts(archives);
        final ConflictCheckResponsePrinter printer = new ConflictCheckResponsePrinter();
        printer.setOutput(new StreamConsumer() {
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
        printer.printResults(response, previewThreshold);
    }
}
