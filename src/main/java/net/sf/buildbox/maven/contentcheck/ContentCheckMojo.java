package net.sf.buildbox.maven.contentcheck;

import java.io.File;
import java.io.IOException;
import java.util.Set;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This MOJO checks an archive content according to an authoritative source. This authoritative source
 * defines set of allowed files in the archive.
 *
 * @author Petr Kozelka (pkozelka@gmail.com)
 * 
 * @goal check
 * @phase verify
 */
public class ContentCheckMojo extends AbstractMojo {

    public final static String DEFAULT_VENDOR_MANIFEST_ENTRY_NAME = "Implementation-Vendor-Id";
    public final static String DEFAULT_CHECK_FILES_PATTERN = "WEB-INF/lib/*.jar";
    
    /**
     * The archive file to be checked
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    protected File archive;

    /**
     * The file with list of expected files.
     * Each line in that file represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     *
     * @parameter default-value="src/main/content.txt"
     */
    protected File contentListing;

    /**
     * If true, stops the build when there is any file missing.
     * @parameter default-value="false"
     */
    protected boolean failOnMissing;

    /**
     * If true, stops the build when there is any unexpected file.
     * @parameter default-value="true"
     */
    protected boolean failOnUnexpected;
    
    /**
     * The vendor identification. This value is used for JAR's manifest checking
     * when {@link #ignoreVendorArchives} is turned on.
     * @parameter
     * 
     * @see #manifestVendorEntry
     */
    protected String vendorId;

    /**
     * The name of manifest entry that holds vendor's identification
     * 
     * @parameter default-value="Implementation-Vendor-Id"
     */
    protected String manifestVendorEntry;

    /**
     * If true, doesn't check vendor JAR files. A vendor JAR file is determined by 
     * a value ({@link #vendorId}) in its manifest key ({@link #manifestVendorEntry}).
     *   
     * @parameter default-value="false"
     */
    protected boolean ignoreVendorArchives;

    /**
     * An Ant like file pattern. If this roperty is present only files matching 
     * that pattern are checked. Otherwise all JAR files are checked.
     * 
     * @parameter default-value="**\/*.jar"
     */
    protected String checkFilesPattern;

    /**
     * Message used to report missing entry - uses the {@link java.util.Formatter} syntax to embed entry name.
     * @parameter default-value="File is expected but not found: %s"
     */
    protected String msgMissing;

    /**
     * Message used to report unexpected entry - uses the {@link java.util.Formatter} syntax to embed entry name.
     * @parameter default-value="Found unexpected file: %s"
     */
    protected String msgUnexpected;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            validateMojoArguments();

            final ContentChecker contentChecker = new ContentChecker(getLog(), ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
            final CheckerOutput output = contentChecker.check(contentListing, archive);

            // report missing entries
            Set<String> missingEntries = output.diffMissingEntries();
            for (String entry : missingEntries) {
                getLog().error(String.format(msgMissing, entry));
            }
            // report unexpected entries
            Set<String> unexpectedEntries = output.diffUnexpectedEntries();
            for (String entry : unexpectedEntries) {
                getLog().error(String.format(msgUnexpected, entry));
            }
            // fail as neccessary, after reporting all detected problems
            if (failOnMissing && ! missingEntries.isEmpty()) {
                throw new MojoFailureException(missingEntries.size() + " expected entries are missing in " + archive);
            }
            if (failOnUnexpected && ! unexpectedEntries.isEmpty()) {
                throw new MojoFailureException(unexpectedEntries.size() + " unexpected entries appear in " + archive);
            }

            getLog().info("Archive file " + archive.getPath() + " has valid content regarding to " + contentListing.getPath());
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void validateMojoArguments() throws MojoExecutionException{
        if(!archive.exists()) {
            throw new MojoExecutionException("Archive file " + archive.getPath() + " you are trying to check doesn't exist.");
        }

        if(!contentListing.exists()) {
            throw new MojoExecutionException("Content listing file  " + contentListing.getPath() + " doesn't exist.");
        }
        
        if(ignoreVendorArchives && (vendorId == null || vendorId.length() == 0)) {
            throw new MojoExecutionException("ignoreVendorArchives is turned on, but 'vendorId' configuration property is missing. Please specify vendorId property in the plugin configuration.");
        }
    }
}