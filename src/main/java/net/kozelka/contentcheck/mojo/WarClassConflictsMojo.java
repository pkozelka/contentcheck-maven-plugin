package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.impl.ClassConflictDetector;
import net.kozelka.contentcheck.conflict.impl.ConflictCheckResponsePrinter;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.util.ArchiveLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Looks for conflicts within the libraries in given sourceFile.
 *
 * @author Petr Kozelka
 * @since 1.0.3
 */
@Mojo(name="warcc", defaultPhase = LifecyclePhase.VERIFY)
public class WarClassConflictsMojo extends AbstractMojo {
    /**
     * If true, no check is performed.
     */
    @Parameter(defaultValue = "false", property = "contentcheck.skip")
    boolean skip;

    /**
     * The archive file to be checked
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.war", property = "sourceFile")
    File sourceFile;

    /**
     * How many class conflicts to list directly. Use <code>-1</code> to list all.
     */
    @Parameter(defaultValue = "5")
    int previewThreshold;

    /**
     * How many overlaps are we tolerating.
     * Useful to ensure that the number is not growing, when you cannot fix everything.
     * @todo replace this with include/exclude lists
     */
    @Parameter(defaultValue = "0")
    int toleratedOverlapCount;

    /**
     * @deprecated Use {@link #toleratedOverlapCount} instead.
     */
    @Deprecated
    @Parameter(defaultValue = "-1")
    int toleratedConflictCount;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Content conflict checking is skipped.");
            return;
        }

        if (toleratedConflictCount > -1) {
            getLog().warn("Parameter 'toleratedConflictCount' is deprecated - use 'toleratedOverlapCount' instead");
            if (toleratedOverlapCount > 0) {
                getLog().warn("Parameter 'toleratedConflictCount' is just a deprecated variant of 'toleratedOverlapCount'. You specified both, using only the latter.");
            } else {
                toleratedOverlapCount = toleratedConflictCount;
            }
        }
        //
        try {
            final ClassConflictDetector ccd = new ClassConflictDetector();
            final List<ArchiveInfo> archives = ArchiveLoader.loadWar(sourceFile);
            final ConflictCheckResponse response = ccd.findConflicts(archives);
            final List<ConflictCheckResponse.ArchiveConflict> archiveConflicts = response.getArchiveConflicts();
            final int totalOverlaps = response.getTotalOverlaps();
            if (archiveConflicts.isEmpty()) {
                getLog().info("No overlaps detected.");
            } else {
                final StreamConsumer consumer = new StreamConsumer() {
                    @Override
                    public void consumeLine(String line) {
                        getLog().error(line);
                    }
                };
                ConflictCheckResponsePrinter.printResults(response, previewThreshold, consumer);
                final String errorMessage = String.format("Found %d overlapping resources in %d archive conflicts in %s",
                    totalOverlaps,
                    archiveConflicts.size(),
                    sourceFile);
                getLog().error(errorMessage);
                if (totalOverlaps > toleratedOverlapCount) {
                    throw new MojoFailureException(errorMessage);
                }
            }
            if (totalOverlaps < toleratedOverlapCount) {
                getLog().warn(String.format("We currently tolerate %d overlaps; please reduce the tolerance to prevent growing mess", toleratedOverlapCount));
            }
        } catch (IOException e) {
            throw new MojoExecutionException(sourceFile.getAbsolutePath(), e);
        }
    }
}
