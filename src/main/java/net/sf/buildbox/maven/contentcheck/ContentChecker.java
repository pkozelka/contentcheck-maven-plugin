package net.sf.buildbox.maven.contentcheck;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * The checker itself thread safe implementation.
 *
 */
public class ContentChecker {
    private static final String JAR_FILE_EXTENSION = "**/*.jar";

    private final Log log;
    private final boolean ignoreVendorArchives;
    private final String vendorId;
    private final String manifestVendorEntry;
    private final String checkFilesPattern;

    public ContentChecker(Log log, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        super();
        this.log = log;
        this.ignoreVendorArchives = ignoreVendorArchives;
        this.vendorId = vendorId;
        this.manifestVendorEntry = manifestVendorEntry;
        this.checkFilesPattern = checkFilesPattern;
    }

    /**
     * Checks an archive content according to an allowed content. 
     * 
     * @param listingFile a file that defines allowed content
     * @param archiveFile an archive to be checked
     * 
     * @return the result of archive check
     * 
     * @throws IOException if something very bad happen
     */
    public CheckerOutput check(final File listingFile, final File archiveFile) throws IOException{
        Set<String> allowedEntries = readListing(listingFile);
        Set<String> archiveContent = readArchive(archiveFile);
        return new CheckerOutput(allowedEntries, archiveContent);
    }

    protected Set<String> readListing(final File listingFile) throws IOException {
        log.info("Reading listing: " + listingFile);
        final Set<String> expectedPaths = new LinkedHashSet<String>();
        InputStream is =  new FileInputStream(listingFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine())!= null) {
                line = line.trim();
                boolean ignoreLine = line.length() == 0 || line.startsWith("#");// we ignore empty and comments lines
                if (!ignoreLine) { 
                    if(expectedPaths.contains(line)) {
                        log.warn("The listing file " + listingFile + "  defines duplicate entry " + line);
                    }
                    expectedPaths.add(line);
                } 
            }
        } finally {
            is.close();
            reader.close();
        }

        return expectedPaths;
    }

    protected Set<String> readArchive(final File archive) throws IOException {
        log.info("Reading archive: " + archive);
        final Set<String> archiveEntries = new  LinkedHashSet<String>();
        final ZipFile zipFile = new ZipFile(archive);
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
        int totalCnt = 0;
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                totalCnt ++;
                final String entryName = entry.getName();
                if (! shouldBeChecked(entryName)) continue;
                
                if(isJarFileExtension(entryName) && ignoreVendorArchives) {
                    InputStream archiveInputStream = zipFile.getInputStream(entry);
                    if(isVendorArchive(entryName, archiveInputStream)) {
                        continue;
                    }
                }
                
                if (! archiveEntries.add(entryName)) {
                    log.error("ERROR: Archive file " + archive + " contains duplicate entry: " + entryName);
                    //TODO: should we just fail here ? or on config option ?
                    //XXX Dagi: i don't think that the archive may have duplicit entries
                }
            }
        } finally {
            zis.close();
            try { 
                zipFile.close();
            } catch(IOException e) {
                // ignored
            }
        }
        log.info(String.format("%s: archive contains %d checked and %d total files", archive, archiveEntries.size(), totalCnt));
        return archiveEntries;
    }

    private boolean shouldBeChecked(String path) {
        return DirectoryScanner.match(checkFilesPattern, path);
    }
    
    private boolean isJarFileExtension(String path) {
        return DirectoryScanner.match(JAR_FILE_EXTENSION, path);
    }
    
    private boolean isVendorArchive(final String jarPath, final InputStream archiveInputStream) throws IOException {
        File tempFile = null;
        try {
            tempFile = copyStreamToTemporaryFile(jarPath, archiveInputStream);
        } finally {	
//DO NOT!!!            archiveInputStream.close();
        }
        return checkArchiveManifest(jarPath, tempFile);
    }

    /**
     * @param jarPath -
     * @param tempJAR -
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