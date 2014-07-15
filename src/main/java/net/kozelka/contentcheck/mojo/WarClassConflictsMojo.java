package net.kozelka.contentcheck.mojo;

import net.kozelka.contentcheck.conflict.ArchiveInfo;
import net.kozelka.contentcheck.conflict.ClassConflictDetector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Looks for conflict within the libraries in given war.
 *
 * @author Petr Kozelka
 */
@Mojo(name="warcc", defaultPhase = LifecyclePhase.TEST)
public class WarClassConflictsMojo extends AbstractMojo {
    /**
     * The archive file to be checked
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.war", property = "war")
    File war;

    /**
     * How many class conflicts to list directly. Use <code>-1</code> to list all.
     */
    @Parameter(defaultValue = "5")
    int previewThreshold;

    /**
     * Stop the build when conflicts are detected.
     */
    @Parameter(defaultValue = "true")
    boolean failOnError;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final ClassConflictDetector ccd = ClassConflictDetector.exploreWar(war);
            final List<ArchiveInfo> conflictingArchives = ccd.getConflictingArchives();
            if (!conflictingArchives.isEmpty()) {
                final String errorMessage = String.format("Found %d conflicting archives in %s", conflictingArchives.size(), war);
                getLog().error(errorMessage);
                ccd.printResults(previewThreshold, new ClassConflictDetector.LineOutput() {
                    @Override
                    public void println(String line) {
                        getLog().error(line);
                    }
                });
                if (failOnError) {
                    throw new MojoFailureException(errorMessage);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(war.getAbsolutePath(), e);
        }
    }
}
