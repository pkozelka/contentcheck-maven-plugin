package net.sf.buildbox.maven.contentcheck;

import java.io.IOException;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This MOJO checks an archive content according to an authoritative source. This authoritative source
 * defines set of allowed files in the archive.
 *
 * @goal check
 * @phase verify
 * @threadSafe
 */
public class ContentCheckMojo extends AbstractArchiveContentMojo {
    
    /**
     * Message used to report missing entry - uses the {@link java.util.Formatter} syntax to embed entry name.
     * @parameter default-value="File is expected but not found: %s"
     */
    private String msgMissing;

    /**
     * Message used to report unexpected entry - uses the {@link java.util.Formatter} syntax to embed entry name.
     * @parameter default-value="Found unexpected file: %s"
     */
    private String msgUnexpected;
    
    /**
     * If true, stops the build when there is any file missing.
     * @parameter default-value="false"
     */
    private boolean failOnMissing;

    /**
     * If true, stops the build when there is any unexpected file.
     * @parameter default-value="true"
     */
    private boolean failOnUnexpected;

    /**
     * If true, modules with POM packaging are skipped (excluded from the content check).
     * 
     * @parameter default-value="false"
     */
    private boolean skipPOMPackaging;

    protected void doExecute() throws IOException, MojoExecutionException, MojoFailureException {
        
        if(skipPOMPackaging && "POM".equalsIgnoreCase(getMavenProject().getPackaging())) {
            log(false, "Skipping content check for project with POM packaging");
            return;
        }
        
        if(!getContentListing().exists()) {
            throw new MojoExecutionException("Content listing file  " + getContentListing().getPath() + " doesn't exist.");
        }
        
        final ContentChecker contentChecker = new ContentChecker(getLog(), isIgnoreVendorArchives(), getVendorId(), getManifestVendorEntry(), getCheckFilesPattern());
        final CheckerOutput output = contentChecker.check(getContentListing(), getSourceFile());

        // report missing entries
        final Set<String> missingEntries = output.diffMissingEntries();
        for (String entry : missingEntries) {
            log(failOnMissing, String.format(msgMissing, entry));
        }
        // report unexpected entries
        final Set<String> unexpectedEntries = output.diffUnexpectedEntries();
        for (String entry : unexpectedEntries) {
            log(failOnUnexpected, String.format(msgUnexpected, entry));
        }
        // error summary
        if (missingEntries.size() > 0) {
            log(failOnMissing, "Missing: " + missingEntries.size() + " entries");
        }
        if (unexpectedEntries.size() > 0) {
            log(failOnUnexpected, "Unexpected: " + unexpectedEntries.size() + " entries");
        }
        // fail as neccessary, after reporting all detected problems
        if (failOnMissing && ! missingEntries.isEmpty()) {
            throw new MojoFailureException(missingEntries.size() + " expected entries are missing in " + getSourceFile());
        }

        if (failOnUnexpected && ! unexpectedEntries.isEmpty()) {
            throw new MojoFailureException(unexpectedEntries.size() + " unexpected entries appear in " + getSourceFile());
        }

        getLog().info("Source " + getSourceFile().getPath() + " has valid content according to " + getContentListing().getPath());
    }

    private void log(boolean error, String message) {
        if (error) {
            getLog().error(message);
        } else {
            getLog().warn(message);
        }
    }
}
