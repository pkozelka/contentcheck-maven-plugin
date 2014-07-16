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
     * The archive file to be checked.
     * You should specify either <i>archive</i> or {@link #directory}.
     * Directory takes a precedence before archive.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}")
    File archive;

    /**
     * The directory to be checked.
     * You should specify either {@link #archive} or <i>directory</i>.
     * Directory parameter has no default value therefore it takes a precedence before {@link #archive}.
     */
    @Parameter
    File directory;

    /**
     * The file with list of approved files.
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
        if (directory != null &&  ! directory.exists()) {
            throw new MojoExecutionException("Directory " + directory.getPath() + " you are trying to check doesn't exist.");
        } else if(!archive.exists()) {
            throw new MojoExecutionException("Archive file " + archive.getPath() + " you are trying to check doesn't exist.");
        }
        
        if(ignoreVendorArchives && (vendorId == null || vendorId.length() == 0)) {
            throw new MojoExecutionException("ignoreVendorArchives is turned on, but 'vendorId' configuration property is missing. Please specify vendorId property in the plugin configuration.");
        }
    }
    
    protected abstract void doExecute() throws IOException, MojoExecutionException, MojoFailureException;

    /**
     * Returns correct source file, directory takes a precedence before archive file.
     * @return directory if not null, archive file otherwise
     * @todo consider if we really need two plugin parameters (directory and archive) when both have same type. I guess one named sourceFile would be enough.
     */
    protected File getSourceFile() {
        return directory != null ? directory : archive;
    }
}
