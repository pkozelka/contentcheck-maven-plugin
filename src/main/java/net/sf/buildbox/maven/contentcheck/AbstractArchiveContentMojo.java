package net.sf.buildbox.maven.contentcheck;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class AbstractArchiveContentMojo extends AbstractMojo {
    // [pk] not sure if these constants make any sense here ...
    public final static String DEFAULT_VENDOR_MANIFEST_ENTRY_NAME = "Implementation-Vendor-Id";
    public final static String DEFAULT_CHECK_FILES_PATTERN = "WEB-INF/lib/*.jar";
    
    /**
     * The archive file to be checked
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    private File archive;

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

    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            doExecute();
        }catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
    
    protected void validateMojoArguments() throws MojoExecutionException{
        if(!archive.exists()) {
            throw new MojoExecutionException("Archive file " + archive.getPath() + " you are trying to check doesn't exist.");
        }
        
        if(ignoreVendorArchives && (vendorId == null || vendorId.length() == 0)) {
            throw new MojoExecutionException("ignoreVendorArchives is turned on, but 'vendorId' configuration property is missing. Please specify vendorId property in the plugin configuration.");
        }
    }
    
    protected abstract void doExecute() throws IOException, MojoExecutionException, MojoFailureException;

    protected File getArchive() {
        return archive;
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
}
