package net.kozelka.contentcheck.mojo;

import java.io.IOException;
import java.util.Set;

import net.kozelka.contentcheck.ContentChecker;
import net.kozelka.contentcheck.CheckerOutput;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Checks the archive content according to an authoritative source. This authoritative source
 * defines set of allowed files in the archive.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class ContentCheckMojo extends AbstractArchiveContentMojo {

    /**
     * If true, no check is performed.
     */
    @Parameter(defaultValue = "false", property = "contentcheck.skip")
    boolean skip;
    
    /**
     * Message used to report missing entry - uses the {@link java.util.Formatter} syntax to embed entry name.
     */
    @Parameter(defaultValue = "File is expected but not found: %s")
    String msgMissing;

    /**
     * Message used to report unexpected entry - uses the {@link java.util.Formatter} syntax to embed entry name.
     */
    @Parameter(defaultValue = "Found unexpected file: %s")
    String msgUnexpected;
    
    /**
     * If true, stops the build when there is any file missing.
     */
    @Parameter(defaultValue = "false")
    boolean failOnMissing;

    /**
     * If true, stops the build when there is any unexpected file.
     */
    @Parameter(defaultValue = "true")
    boolean failOnUnexpected;

    /**
     * If true, modules with POM packaging are skipped (excluded from the content check).
     */
    @Parameter(defaultValue = "false")
    boolean skipPOMPackaging;

    protected void doExecute() throws IOException, MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info("Content checking is skipped.");
            return;
        }

        if(!contentListing.exists()) {
            getLog().info("Skipping - file does not exist: " + contentListing);
            return;
        }

        assertSourceFileExists();

        final ContentChecker contentChecker = new ContentChecker(getLog(), ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
        final CheckerOutput output = contentChecker.check(contentListing, sourceFile);

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
            throw new MojoFailureException(missingEntries.size() + " expected entries are missing in " + sourceFile);
        }

        if (failOnUnexpected && ! unexpectedEntries.isEmpty()) {
            throw new MojoFailureException(unexpectedEntries.size() + " unexpected entries appear in " + sourceFile);
        }

        getLog().info("Source " + sourceFile.getAbsolutePath() + " has valid content according to " + contentListing.getAbsolutePath());
    }

    private void log(boolean error, String message) {
        if (error) {
            getLog().error(message);
        } else {
            getLog().warn(message);
        }
    }
}
