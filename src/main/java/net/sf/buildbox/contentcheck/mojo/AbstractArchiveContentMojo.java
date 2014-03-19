package net.sf.buildbox.contentcheck.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

public abstract class AbstractArchiveContentMojo extends AbstractMojo {

    /**
     * The archive file to be checked.
     * You should specify either {@link #archive} or {@link #directory}.
     * Directory takes a precedence before archive.
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    private File archive;

    /**
     * The directory to be checked.
     * You should specify either {@link #archive} or {@link #directory}.
     * Directory parameter has no default value therefore it takes a precedence before {@link #archive}.
     *
     * @parameter
     */
    private File directory;

    /**
     * The file with list of expected files.
     * Each line in that file represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     *
     * @parameter default-value="src/main/content.txt"
     */
    private File contentListing;

    
    /**
     * The vendor identification. This value is used for JAR's manifest checking
     * when {@link #ignoreVendorArchives} is turned on.
     * @parameter
     * 
     * @see #manifestVendorEntry
     */
    private String vendorId;

    /**
     * The name of manifest entry that holds vendor's identification
     * 
     * @parameter default-value="Implementation-Vendor-Id"
     */
    private String manifestVendorEntry;

    /**
     * If true, doesn't check vendor JAR files. A vendor JAR file is determined by 
     * a value ({@link #vendorId}) in its manifest key ({@link #manifestVendorEntry}).
     *   
     * @parameter default-value="false"
     */
    private boolean ignoreVendorArchives;

    /**
     * An Ant like file pattern. If this roperty is present only files matching 
     * that pattern are checked. Otherwise all JAR files are checked.
     * 
     * @parameter default-value="**\/*.jar"
     */
    private String checkFilesPattern;

    /**
     * The Maven Project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;

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
     */
    protected File getSourceFile() {
        return directory != null ? directory : archive;
    }

    protected File getContentListing() {
        return contentListing;
    }

    protected String getVendorId() {
        return vendorId;
    }

    protected String getManifestVendorEntry() {
        return manifestVendorEntry;
    }

    protected boolean isIgnoreVendorArchives() {
        return ignoreVendorArchives;
    }

    protected String getCheckFilesPattern() {
        return checkFilesPattern;
    }

    public MavenProject getMavenProject() {
        return project;
    }
}
