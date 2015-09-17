package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.expect.impl.ContentCollector;
import net.kozelka.contentcheck.expect.impl.VendorFilter;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.expect.util.ExpectUtils;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Scans content listing of archive specified by {@link #sourceFile}
 * and writes it to file specified into {@link #contentListing}.
 * Only entities matching criteria defined by {@link #checkFilesPattern}
 * and {@link #ignoreVendorArchives} are generated.
 * @since 1.0.1
 */
//TODO * @deprecated use goal <code>setup</code> (class {@link SetupMojo}) instead
//TODO @Deprecated
@Mojo(name = "generate")
public class ContentListingGeneratorMojo extends AbstractArchiveContentMojo {

    /**
     * The file with list of approved files. If such file does not exist, the check is skipped. This enables multimodule use.
     * Each line in represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     */
    @Parameter(defaultValue = "${basedir}/approved-content.txt")
    File contentListing;

    /**
     * This parameter allows overwriting existing listing file.
     */
    @Parameter(defaultValue = "false", property = "overwriteExistingListing")
    boolean overwriteExistingListing;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if(!overwriteExistingListing && contentListing.exists()) {
            //TODO: use alternate output file (like target/contentcheck-maven-plugin/approved-content.txt) and fail after finishing the generation (issue #15)
            throw new MojoFailureException(String.format("Content listing file '%s' already exists. Please set overwriteExistingListing property in plugin configuration or delete this listing file.", contentListing.getPath()));
        }

        try {
            if (ignoreVendorArchives) {
                getLog().warn(String.format("Archives of vendor '%s', indicated by manifest entry '%s', will not be added to the list", vendorId, manifestVendorEntry));
            }
            final List<ActualEntry> sourceEntries = scanActualEntries();
            getLog().info(String.format("Generated %d entries.", sourceEntries.size()));
//            getLog().info(String.format("The source contains %d entries, but only %d matches the plugin configuration criteria.", count, sourceEntries.size()));

            ExpectUtils.generateListing(sourceEntries, contentListing);
            getLog().info(String.format("The listing file '%s' has been successfully generated.", contentListing));
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    // TODO: following methods should be moved to/unified with ContentChecker class
    private List<ActualEntry> scanActualEntries() throws IOException {
        final ContentIntrospector introspector = VendorFilter.createIntrospector(new MyIntrospectionListener(getLog()),
            ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
        introspector.setSourceFile(sourceFile);
        final List<ActualEntry> actualEntries = new ArrayList<ActualEntry>();
        final ContentIntrospector.Events collector = new ContentCollector(actualEntries);
        introspector.getEvents().addListener(collector);
        introspector.walk();
        return actualEntries;
    }

}
