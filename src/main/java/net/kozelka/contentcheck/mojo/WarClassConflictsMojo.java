package net.kozelka.contentcheck.mojo;

import net.kozelka.contentcheck.war.ArchiveInfo;
import net.kozelka.contentcheck.war.ClassConflictDetector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This mojo looks for conflicts within the libraries in given war.
 *
 * @author Petr Kozelka
 * @goal warcc
 * @phase test
 */
public class WarClassConflictsMojo extends AbstractMojo {
    /**
     * The archive file to be checked
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.war" expression="${war}"
     */
    private File war;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final ClassConflictDetector ccd = ClassConflictDetector.exploreWar(war);
            final List<ArchiveInfo> conflictingArchives = ccd.getConflictingArchives();
            if (!conflictingArchives.isEmpty()) {
                final String errorMessage = String.format("Found %d conflicting archives in %s", conflictingArchives.size(), war);
                getLog().error(errorMessage);
                for (ArchiveInfo conflict : conflictingArchives) {
                    getLog().error(conflict.getKey());
                }
                throw new MojoFailureException(errorMessage);
            }
        } catch (IOException e) {
            throw new MojoExecutionException(war.getAbsolutePath(), e);
        }
    }
}
