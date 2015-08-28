package net.kozelka.contentcheck.mojo;

import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.impl.ClassConflictDetector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
     * How many conflicts are we tolerating.
     * Useful to ensure that the number is not growing, when you cannot fix everything.
     * @todo replace this with include/exclude lists
     */
    @Parameter(defaultValue = "0")
    int toleratedConflictCount;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Content checking is skipped.");
            return;
        }

        try {
            final ClassConflictDetector ccd = new ClassConflictDetector();
            final ConflictCheckResponse response = ccd.exploreWar(sourceFile);
            final List<ArchiveInfo> conflictingArchives = response.getConflictingArchives();
            final int totalConflicts;
            if (conflictingArchives.isEmpty()) {
                totalConflicts = 0;
                getLog().info("No conflicts detected.");
            } else {
                totalConflicts = ccd.printResults(response, previewThreshold, new StreamConsumer() {
                    @Override
                    public void consumeLine(String line) {
                        getLog().error(line);
                    }
                });
                final String errorMessage = String.format("Found %d conflicts in %d archives in %s", totalConflicts, conflictingArchives.size(), sourceFile);
                getLog().error(errorMessage);
                if (totalConflicts > toleratedConflictCount) {
                    throw new MojoFailureException(errorMessage);
                }
            }
            if (totalConflicts < toleratedConflictCount) {
                getLog().warn(String.format("We currently tolerate %d conflicts; please reduce the tolerance to prevent growing conflicts", toleratedConflictCount));
            }
        } catch (IOException e) {
            throw new MojoExecutionException(sourceFile.getAbsolutePath(), e);
        }
    }
}
