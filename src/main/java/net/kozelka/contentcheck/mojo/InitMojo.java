package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ArchiveConflict;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.impl.ClassConflictAnalyzer;
import net.kozelka.contentcheck.conflict.impl.ClassConflictPrinter;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.util.ArchiveLoader;
import net.kozelka.contentcheck.expect.impl.ContentCollector;
import net.kozelka.contentcheck.expect.impl.VendorFilter;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.expect.util.ExpectUtils;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Convenient way to start using contentcheck plugin, with all its capabilities enabled.
 * Use it to make all following at once:
 * <ul>
 *     <li>generate the <code>approved-content.txt</code> file to match current war content</li>
 *     <li>count war overlaps</li>
 *     <li>TODO: show resolution hints</li>
 *     <li>TODO: generate POM fragment with complete configuration (in target/...)</li>
 *     <li>TODO: operate on each module of multi-module project</li>
 *     <li>TODO: optionally, adjust POM configuration: `-DeditPom=true`</li>
 * </ul>
 */
@Mojo(name = "init")
public class InitMojo extends AbstractMojo {

    /**
     * The file with list of approved files. If such file does not exist, the check is skipped. This enables multimodule use.
     * Each line in represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     */
    @Parameter(defaultValue = "${basedir}/approved-content.txt")
    File contentListing;

    /**
     * The Maven Project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

    private boolean reportJarPairs = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final Build build = project.getBuild();
        final File sourceFile = new File(build.getDirectory(), build.getFinalName() + "." + project.getArtifact().getType());
        try {
            // report class overlaps / conflicts
            final ClassConflictAnalyzer ccd = new ClassConflictAnalyzer();
            final List<ArchiveInfo> archives = ArchiveLoader.loadWar(sourceFile);
            final ClassConflictReport report = ccd.analyze(archives);
            final StreamConsumer output = new StreamConsumer() {
                public void consumeLine(String line) {
                    getLog().error(line);
                }
            };
            if (reportJarPairs) {
                final ClassConflictPrinter printer = new ClassConflictPrinter();
                printer.setPreviewThreshold(5);
                printer.setOutput(output);
                printer.print(report);
            }

            // list source entries into `approved-content.txt`
            final List<ActualEntry> sourceEntries = scanActualEntries(sourceFile);
            getLog().info(String.format("Generated %d entries.", sourceEntries.size()));
//            getLog().info(String.format("The source contains %d entries, but only %d matches the plugin configuration criteria.", count, sourceEntries.size()));

            ExpectUtils.generateListing(sourceEntries, contentListing);
            getLog().info(String.format("The listing file '%s' has been successfully generated.", contentListing));
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    // TODO: following methods should be moved to/unified with ContentChecker class
    private List<ActualEntry> scanActualEntries(File sourceFile) throws IOException {
//        getLog().warn(String.format("Archives of vendor '%s', indicated by manifest entry '%s', will not be added to the list", vendorId, manifestVendorEntry));
        final ContentIntrospector introspector = VendorFilter.createIntrospector(new MyIntrospectionListener(getLog()),
            true, project.getGroupId(), VendorFilter.DEFAULT_VENDOR_MANIFEST_ENTRY_NAME, "**/*.jar");
        getLog().info("Scanning " + sourceFile);
        introspector.setSourceFile(sourceFile);
        introspector.getEvents().addListener(new MyIntrospectionListener(getLog()));
        final List<ActualEntry> actualEntries = new ArrayList<ActualEntry>();
        final ContentIntrospector.Events collector = new ContentCollector(actualEntries);
        introspector.getEvents().addListener(collector);
        introspector.walk();
        return actualEntries;
    }

}
