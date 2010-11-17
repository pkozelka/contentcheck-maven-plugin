package net.sf.buildbox.maven.contentcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * The checker itself thread safe implementation.
 *
 */
public class ContentChecker {
    private static final String JAR_FILE_EXTENSION = "**/*.jar";
    private final PathMatcher pathMatcher = new PathMatcher();

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
     * @throws MojoExecutionException if something very bad happen
     */
    public CheckerOutput check(final File listingFile, final File archiveFile) throws MojoExecutionException{
        Set<String> allowedEntries = readListing(listingFile);
        Set<String> archiveContent = readArchive(archiveFile);
        return new CheckerOutput(allowedEntries, archiveContent);
    }

    protected Set<String> readListing(final File listingFile) throws MojoExecutionException {
        log.info("Reading listing: " + listingFile);
        final Set<String> expectedPaths = new LinkedHashSet<String>();
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is =  new FileInputStream(listingFile);
            reader = new BufferedReader(new InputStreamReader(is));
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
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Cannot read file " + listingFile +  " because doesn't exist.");
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read content of file " + listingFile + ". This file defines allowed content for checked archive.", e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(reader);
        }

        return expectedPaths;
    }

    protected Set<String> readArchive(final File archive) throws MojoExecutionException {
        log.info("Reading archive: " + archive);
        final Set<String> archiveEntries = new  LinkedHashSet<String>();
        ZipFile zipFile = null;
        ZipInputStream zis = null;
        try {
            zipFile = new ZipFile(archive);
            zis = new ZipInputStream(new FileInputStream(archive));
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
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
        } catch(IOException e) {
            throw new MojoExecutionException("Cannot read archive " + archive.getPath(), e);
        } finally {
            IOUtils.closeQuietly(zis);
            try { 
                zipFile.close();
            } catch(IOException e) {}
        }
        return archiveEntries;
    }

    private boolean shouldBeChecked(String path) {
        return pathMatcher.match(checkFilesPattern, path);
    }
    
    private boolean isJarFileExtension(String path) {
        return pathMatcher.match(JAR_FILE_EXTENSION, path);
    }
    
    private boolean isVendorArchive(final String jarPath, final InputStream archiveInputStream) throws MojoExecutionException {
        File tempFile = null;
        try {
            tempFile = copyStreamToTemporaryFile(jarPath, archiveInputStream);
        } finally {	
            IOUtils.closeQuietly(archiveInputStream);
        }
        return checkArchiveManifest(jarPath, tempFile);
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

    private File copyStreamToTemporaryFile(final String jarPath, final InputStream archiveInputStream) throws MojoExecutionException {
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            log.debug("Checking " + jarPath + " to be a vendor archive");
            tempFile = File.createTempFile(UUID.randomUUID().toString(), "jar");
            fos = new  FileOutputStream(tempFile);
            IOUtils.copy(archiveInputStream, fos);
        } catch(IOException e) {
            throw new MojoExecutionException("Cannot create temporary copy of JAR "  + jarPath, e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
        return tempFile;
    }
}