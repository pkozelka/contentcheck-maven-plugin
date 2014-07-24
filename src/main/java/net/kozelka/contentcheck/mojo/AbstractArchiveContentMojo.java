package net.kozelka.contentcheck.mojo;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

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
     * The vendor identification. This value is used for JAR's manifest checking
     * when {@link #ignoreVendorArchives} is turned on.
     * Using the same groupId for all modules in your project can be beneficial for proper verdor archive detection.
     * @see #manifestVendorEntry
     */
    @Parameter(defaultValue = "${project.groupId}")
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

    protected void assertSourceFileExists() throws MojoExecutionException {
        if (sourceFile == null) {
            sourceFile = directory;
            if (sourceFile != null) {
                getLog().warn("Parameter 'directory' is deprecated, please use 'sourceFile' instead.");
            } else {
                sourceFile = archive;
                if (sourceFile != null) {
                    getLog().warn("Parameter 'archive' is deprecated, please use 'sourceFile' instead.");
                }
            }
        }
        if (sourceFile == null) {
            throw new MojoExecutionException("Parameter 'sourceFile' is required.");
        }
        if (!sourceFile.exists()) {
            throw new MojoExecutionException("Archive file or directory " + sourceFile.getAbsolutePath() + " you are trying to check doesn't exist.");
        }
    }
}
