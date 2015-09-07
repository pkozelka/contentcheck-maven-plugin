package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.expect.model.ApprovedEntry;
import net.kozelka.contentcheck.expect.impl.ContentChecker;
import net.kozelka.contentcheck.expect.api.ApproverReport;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Checks the archive content according to an authoritative source. This authoritative source
 * defines set of allowed files in the archive.
 * @since 1.0.0
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class ContentCheckMojo extends AbstractArchiveContentMojo {

    /**
     * If true, no check is performed.
     */
    @Parameter(defaultValue = "false", property = "contentcheck.skip")
    boolean skip;

    /**
     * The file with list of approved files. If such file does not exist, the check is skipped. This enables multimodule use.
     * Each line in represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     */
    @Parameter(defaultValue = "${basedir}/approved-content.txt")
    File contentListing;

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

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info("Content checking is skipped.");
            return;
        }

        if(!contentListing.exists()) {
            getLog().info("Skipping - file does not exist: " + contentListing);
            return;
        }

        assertSourceFileExists();

        try {
            final MyIntrospectionListener introspectionListener = new MyIntrospectionListener(getLog());
            final ContentIntrospector introspector = ContentIntrospector.create(introspectionListener, ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
            introspector.setSourceFile(sourceFile);
            final ContentChecker contentChecker = new ContentChecker();
            contentChecker.getEvents().addListener(new MyContentCheckerListener(getLog()));
            contentChecker.setIntrospector(introspector);
            //
            getLog().info("Reading listing: " + contentListing);
            final ApproverReport output = contentChecker.check(contentListing);

            // report missing entries
            final Set<ApprovedEntry> missingEntries = output.getMissingEntries();
            for (ApprovedEntry missing : missingEntries) {
                log(failOnMissing, String.format(msgMissing, missing));
            }
            // report unexpected entries
            final Set<ActualEntry> unexpectedEntries = output.getUnexpectedEntries();
            for (ActualEntry actualEntry : unexpectedEntries) {
                log(failOnUnexpected, String.format(msgUnexpected, actualEntry.getUri()));
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
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void log(boolean error, String message) {
        if (error) {
            getLog().error(message);
        } else {
            getLog().warn(message);
        }
    }
}
