package net.sf.buildbox.maven.contentcheck.introspection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * This class provides archive's content introspection in a template manner. Please
 * see {@link DefaultIntrospector} that provides archive's content as a set of paths.
 * 
 * @see #readArchive(File)
 * @see DefaultIntrospector
 */
public abstract class AbstractArchiveIntrospector {
    private static final String JAR_FILE_EXTENSION = "**/*.jar";

    private final Log log;
    private final boolean ignoreVendorArchives;
    private final String vendorId;
    private final String manifestVendorEntry;
    private final String checkFilesPattern;

    public AbstractArchiveIntrospector(Log log, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        this.log = log;
        this.ignoreVendorArchives = ignoreVendorArchives;
        this.vendorId = vendorId;
        this.manifestVendorEntry = manifestVendorEntry;
        this.checkFilesPattern = checkFilesPattern;
    }

    /**
     * Starts reading archive's content entry by entry. If an entry matches {@link #checkFilesPattern}
     * and is not a vendor archive (in case of {@link #ignoreVendorArchives} is <code>true</code>)
     * the entry will be delegated to the method {@link #processEntry(ZipEntry)}
     * for further processing.
     * 
     * @param archive an archive to be read
     * 
     * @return the number of read entities.
     * 
     * @see #processEntry(ZipEntry)
     */
    public final int readArchive(final File archive) throws IOException {
        log.info("Reading archive: " + archive);
        final ZipFile zipFile = new ZipFile(archive);
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
        int totalCnt = 0;
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                totalCnt ++;
                final String entryName = entry.getName();
                
                if (!shouldBeChecked(entryName)) {
                    log.debug(String.format("Skipping entry '%s' doesn't match with pattern '%s'", entryName, checkFilesPattern));
                    continue;
                }
                
                if(isJarFileExtension(entryName) && ignoreVendorArchives) {
                    if(isVendorArchive(entry, zipFile)) {
                        log.debug(String.format("Skipping entry '%s' it's a vendor archive", entryName));
                        continue;
                    }
                }

                processEntry(entry);
            }
        } finally {
            try {
                zis.close();
            } finally {
                zipFile.close();
            }
        }
        return totalCnt;
    }

    /**
     * Process a given entry. This method is intended to be implemented by subclasses.
     * 
     * @param entry an entry to be processed
     */
    public abstract void processEntry(ZipEntry entry) throws IOException;

    /**
     * Checks whether a path point to a JAR file or not.
     * @param path the path to be checked
     * @return <code>true</code> - path to JAR otherwise <code>false</code>
     */
    protected boolean isJarFileExtension(String path) {
        return SelectorUtils.matchPath(JAR_FILE_EXTENSION, path);
    }

    /**
     * @return the log
     */
    protected Log getLog() {
        return log;
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

    private boolean isVendorArchive(final ZipEntry entry, final ZipFile zipFile) throws IOException {
        final InputStream archiveInputStream = zipFile.getInputStream(entry);
        try {
            final File tempFile = copyStreamToTemporaryFile(entry.getName(), archiveInputStream);
            return checkArchiveManifest(entry.getName(), tempFile);
        } finally {
            archiveInputStream.close();
        }
    }

    /**
     * @return true when vendorId matches with jar's manifest otherwise false
     */
    private boolean checkArchiveManifest(final String jarPath, File tempJAR) {
        try {
            JarFile jarFile = new JarFile(tempJAR);
            Manifest manifest = jarFile.getManifest();
            if(manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                if(mainAttributes != null) {
                    String vendor = mainAttributes.getValue(manifestVendorEntry);
                    return vendorId.equals(vendor);
                }
            }
        } catch (IOException e) {
            log.warn("Cannot check MANIFEST.MF file in JAR archive " + jarPath, e);
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
