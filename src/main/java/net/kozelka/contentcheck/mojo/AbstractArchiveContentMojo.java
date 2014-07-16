package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractArchiveContentMojo extends AbstractMojo {

    /**
     * The archive file or directory to be checked.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}")
    File sourceFile;

    /**
     * @deprecated use {@link #sourceFile} instead
     */
    @Deprecated @Parameter
    private File archive;

    /**
     * @deprecated use {@link #sourceFile} instead
     */
    @Deprecated @Parameter
    private File directory;

    /**
     * The file with list of approved files. If such file does not exist, the check is skipped. This enables multimodule use.
     * Each line in represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     */
    @Parameter(defaultValue = "${basedir}/approved-content.txt")
    File contentListing;

    
    /**
     * The vendor identification. This value is used for JAR's manifest checking
     * when {@link #ignoreVendorArchives} is turned on.
     * @see #manifestVendorEntry
     */
    @Parameter
    String vendorId;

    /**
     * The name of manifest entry that holds vendor's identification
     */
    @Parameter(defaultValue = "Implementation-Vendor-Id")
    String manifestVendorEntry;

    /**
     * If true, doesn't check vendor JAR files. A vendor JAR file is determined by 
     * a value ({@link #vendorId}) in its manifest key ({@link #manifestVendorEntry}).
     */
    @Parameter(defaultValue = "false")
    boolean ignoreVendorArchives;

    /**
     * Ant like file pattern selecting archive entries (files) to include in the check.
     */
    @Parameter(defaultValue = "**/*.jar")
    String checkFilesPattern;

    /**
     * The Maven Project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            doExecute();
        }catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
    
    protected void validateMojoArguments() throws MojoExecutionException{
        if(ignoreVendorArchives && (vendorId == null || vendorId.length() == 0)) {
            throw new MojoExecutionException("ignoreVendorArchives is turned on, but 'vendorId' configuration property is missing. Please specify vendorId property in the plugin configuration.");
        }
    }

    protected void assertSourceFileExists() throws MojoExecutionException {
        if (directory != null) {
            getLog().warn("Parameter 'directory' is deprecated, please use 'sourceFile' instead.");
            if (!directory.exists()) {
                throw new MojoExecutionException("Directory " + directory.getAbsolutePath() + " you are trying to check doesn't exist.");
            }
            sourceFile = directory;
        } else if (archive != null) {
            getLog().warn("Parameter 'archive' is deprecated, please use 'sourceFile' instead.");
            if (!archive.exists()) {
                throw new MojoExecutionException("Archive file " + archive.getAbsolutePath() + " you are trying to check doesn't exist.");
            }
            sourceFile = archive;
        }
        if (!sourceFile.exists()) {
            throw new MojoExecutionException("Archive file or directory " + archive.getAbsolutePath() + " you are trying to check doesn't exist.");
        }
    }

    protected abstract void doExecute() throws IOException, MojoExecutionException, MojoFailureException;
}
