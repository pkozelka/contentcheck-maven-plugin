package net.kozelka.contentcheck.conflict.impl;

import java.util.Collection;
import net.kozelka.contentcheck.conflict.api.ArchiveConflict;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;

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

}
