package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * This introspector captures all passed entities by their paths.
 *
 * @see #sourceEntries
 */
public class ContentIntrospector {
    private final Set<String> sourceEntries = new LinkedHashSet<String>();
    private IntrospectionListener listener;
    private boolean ignoreVendorArchives = false;
    private String vendorId = "com.example";
    private String manifestVendorEntry = "Implementation-Vendor-Id";
    private String checkFilesPattern = "**/*.jar";

    public static ContentIntrospector create(IntrospectionListener listener, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        final ContentIntrospector contentIntrospector = new ContentIntrospector();
        contentIntrospector.setListener(listener);
        // todo think about vendor detector
        contentIntrospector.setIgnoreVendorArchives(ignoreVendorArchives);
        contentIntrospector.setVendorId(vendorId);
        contentIntrospector.setManifestVendorEntry(manifestVendorEntry);
        // todo use FilenameFilter?
        contentIntrospector.setCheckFilesPattern(checkFilesPattern);
        return contentIntrospector;
    }

    public void setListener(IntrospectionListener listener) {
        this.listener = listener;
    }

    public void setIgnoreVendorArchives(boolean ignoreVendorArchives) {
        this.ignoreVendorArchives = ignoreVendorArchives;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setManifestVendorEntry(String manifestVendorEntry) {
        this.manifestVendorEntry = manifestVendorEntry;
    }

    public void setCheckFilesPattern(String checkFilesPattern) {
        this.checkFilesPattern = checkFilesPattern;
    }

    private void processEntry(String entry) throws IOException {
        sourceEntries.add(entry);
    }

    /**
     * @return the entries found in source
     */
    public Set<String> getEntries() {
        return sourceEntries;
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
        listener.readingSourceFile(sourceFile);
        final IntrospectorInputStrategy inputStrategy;
        if (sourceFile.isDirectory()) {
            inputStrategy = new DirectoryIntrospectorStrategy();
        } else {
            inputStrategy = new ZipArchiveIntrospectorStrategy();
        }

        int totalCnt = 0;
        for (String entry : inputStrategy.list(sourceFile)) {
            totalCnt++;
            if (!shouldBeChecked(entry)) {
                listener.skippingEntryNotMatching(entry);
                continue;
            }

            final boolean isJar = entry.endsWith(".jar");
            if(isJar && ignoreVendorArchives) {
                //
                if(isVendorArchive(entry, inputStrategy.getInputStream(sourceFile, entry))) {
                    listener.skippingEntryOwnModule(entry);
                    continue;
                }
            }

            processEntry(entry);
        }

        return totalCnt;
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
            listener.cannotCheckManifest(jarPath, e);
        } finally {
            if(jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    listener.cannotClose(jarPath, e);
                }
            }
        }
        return false;
    }

    private File copyStreamToTemporaryFile(final String jarPath, final InputStream archiveInputStream) throws IOException {
        final File tempFile = File.createTempFile(UUID.randomUUID().toString(), "jar");
        final FileOutputStream fos = new  FileOutputStream(tempFile);
        try {
            listener.checkingInTmpfile(jarPath, tempFile);
            IOUtil.copy(archiveInputStream, fos);
            return tempFile;
        } finally {
            fos.close();
        }
    }
}
