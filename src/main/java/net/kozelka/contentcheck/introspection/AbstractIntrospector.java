package net.kozelka.contentcheck.introspection;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.SelectorUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This class provides archive's content introspection in a template manner. Please
 * see {@link DefaultIntrospector} that provides archive's content as a set of paths.
 *
 * @todo keep maven dependencies in 'mojo' subpackage
 * @see #readEntries(File)
 * @see DefaultIntrospector
 */
abstract class AbstractIntrospector {
    private static final String JAR_FILE_EXTENSION = "**/*.jar";

    private final Log log;
    private final boolean ignoreVendorArchives;
    private final String vendorId;
    private final String manifestVendorEntry;
    private final String checkFilesPattern;
    //TODO: option to ignore snapshots

    public AbstractIntrospector(Log log, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        this.log = log;
        this.ignoreVendorArchives = ignoreVendorArchives;
        this.vendorId = vendorId;
        this.manifestVendorEntry = manifestVendorEntry;
        this.checkFilesPattern = checkFilesPattern;
    }

    /**
     * Starts reading {@code sourceFile}'s content entry by entry. If an entry matches {@link #checkFilesPattern}
     * and is not a vendor archive (in case of {@link #ignoreVendorArchives} is <code>true</code>)
     * the entry will be delegated to the method {@link #processEntry(String)}
     * for further processing.
     * 
     * @param sourceFile a source file to be read, typically an archive or directory
     * 
     * @return the number of read entities.
     * 
     * @see #processEntry(String)
     */
    public final int readEntries(final File sourceFile) throws IOException {
        log.info("Reading source file: " + sourceFile);
        final IntrospectorInputStrategy inputStrategy;
        if (sourceFile.isDirectory()) {
            inputStrategy = new DirectoryIntrospectorStrategy();
        } else {
            inputStrategy = new ZipArchiveIntrospectorStrategy();
        }

        int totalCnt = 0;
        for (String entry : inputStrategy.readAllEntries(sourceFile)) {
            totalCnt++;
            if (!shouldBeChecked(entry)) {
                log.debug(String.format("Skipping entry '%s' doesn't match with pattern '%s'", entry, checkFilesPattern));
                continue;
            }

            if(isJarFileExtension(entry) && ignoreVendorArchives) {
                //
                if(isVendorArchive(entry, inputStrategy.readEntryData(sourceFile, entry))) {
                    log.debug(String.format("Skipping entry '%s' it's a vendor archive", entry));
                    continue;
                }
            }

            processEntry(entry);
        }

        return totalCnt;
    }

    /**
     * Process a given entry. This method is intended to be implemented by subclasses.
     * 
     * @param entry an entry to be processed
     */
    public abstract void processEntry(String entry) throws IOException;

    /**
     * Checks whether a path point to a JAR file or not.
     * @param path the path to be checked
     * @return <code>true</code> - path to JAR otherwise <code>false</code>
     */
    protected boolean isJarFileExtension(String path) {
        return SelectorUtils.matchPath(JAR_FILE_EXTENSION, path);
    }

    /**
     * @return the ignoreVendorArchives
     */
    protected boolean isIgnoreVendorArchives() {
        return ignoreVendorArchives;
    }

    /**
     * @return the vendorId
     */
    protected String getVendorId() {
        return vendorId;
    }

    /**
     * @return the manifestVendorEntry
     */
    protected String getManifestVendorEntry() {
        return manifestVendorEntry;
    }

    /**
     * @return the checkFilesPattern
     */
    protected String getCheckFilesPattern() {
        return checkFilesPattern;
    }

    private boolean shouldBeChecked(String path) {
        return SelectorUtils.matchPath("/" + checkFilesPattern, "/" + path);
    }

    private boolean isVendorArchive(final String entryName, final InputStream archiveEntryData) throws IOException {
        try {
            final File tempFile = copyStreamToTemporaryFile(entryName, archiveEntryData);
            tempFile.deleteOnExit();
            final boolean vendorArchive = checkArchiveManifest(entryName, tempFile);
            tempFile.delete();//only for sure if the plugin is used in long live JVM
            return vendorArchive;
        } finally {
            archiveEntryData.close();
        }
    }

    /**
     * @return true when vendorId matches with jar's manifest otherwise false
     */
    private boolean checkArchiveManifest(final String jarPath, File tempJAR) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(tempJAR);
            final Manifest manifest = jarFile.getManifest();
            if(manifest != null) {
                final Attributes mainAttributes = manifest.getMainAttributes();
                if(mainAttributes != null) {
                    final String vendor = mainAttributes.getValue(manifestVendorEntry);
                    return vendorId.equals(vendor);
                }
            }

        } catch (IOException e) {
            log.warn("Cannot check MANIFEST.MF file in JAR archive " + jarPath, e);
        } finally {
            if(jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.warn("Cannot close temporary JAR file " + jarPath,e);
                }
            }
        }
        return false;
    }

    private File copyStreamToTemporaryFile(final String jarPath, final InputStream archiveInputStream) throws IOException {
        final File tempFile = File.createTempFile(UUID.randomUUID().toString(), "jar");
        final FileOutputStream fos = new  FileOutputStream(tempFile);
        try {
            log.debug("Checking " + jarPath + " to be a vendor archive, using tempfile " + tempFile);
            IOUtil.copy(archiveInputStream, fos);
            return tempFile;
        } finally {
            fos.close();
        }
    }
}
