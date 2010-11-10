package net.sf.buildbox.maven.contentcheck;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Check an archive's content against given content listing.
 *
 * @author Petr Kozelka (pkozelka@gmail.com)
 * @goal check
 * @phase verify
 */
public class ContentCheckMojo extends AbstractMojo {

    /**
     * The archive file to check
     *
     * @parameter default-value="${project.artifactFile}"
     */
    File archive;

    /**
     * Pointer to the file with list of expected files.
     * Each line in that file represents one pathname entry.
     * Empty lines and comments (starting with '#') are ignored.
     *
     * @parameter default-value="src/main/content.txt"
     */
    File contentListing;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Reading listing: " + contentListing);
            final Set<String> expectedEntries = readListing(contentListing);
            getLog().info("Reading archive: " + archive);
            final Set<String> archiveEntries = readArchive(archive);
            // report missing entries
            final Set<String> missingEntries = new HashSet<String>(expectedEntries);
            missingEntries.removeAll(archiveEntries);
            // report unexpected entries
            final Set<String> unexpectedEntries = new HashSet<String>(archiveEntries);
            unexpectedEntries.removeAll(expectedEntries);
            // todo: fail as neccessary
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Set<String> readArchive(File archive) throws IOException {
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
        final Set<String> archiveEntries = new HashSet<String>();
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                final String entryName = entry.getName();
                if (entry.isDirectory()) continue;
                if (! shouldBeChecked(entryName)) continue;
                if (! archiveEntries.add(entryName)) {
                    getLog().error("ERROR: Archive file " + archive + " contains duplicate entry: " + entryName);
                    //TODO: should we just fail here ? or on config option ?
                }
            }
        } finally {
            zis.close();
        }
        return archiveEntries;
    }

    private static boolean shouldBeChecked(String name) {
        return name.toLowerCase().endsWith(".jar");
    }

    private static Set<String> readListing(File listingFile) throws IOException {
        final Set<String> expectedPaths = new HashSet<String>();
        final InputStream is = new FileInputStream(listingFile);
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                if (line.trim().length() == 0) {
                    // we ignore empty lines
                } else if (line.startsWith("#")) {
                    // we ignore comments
                } else if (expectedPaths.add(line)) {
                    line = reader.readLine();
                } else {
                    // this helps to keep the listing free of duplicates
                    throw new IllegalStateException(listingFile + " : duplicate entry encountered: " + line);
                }
            }
        } finally {
            is.close();
        }

        return expectedPaths;
    }

}
