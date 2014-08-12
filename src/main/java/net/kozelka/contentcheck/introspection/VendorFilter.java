package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.codehaus.plexus.util.IOUtil;

/**
 * Determines if an entry is matching given vendor.
 *
 * @author Petr Kozelka
 */
public class VendorFilter implements ContentIntrospector.EntryContentFilter {

    private final String vendorId;
    private final String manifestVendorEntry;
    private final ContentIntrospector.IntrospectionListener listener;

    VendorFilter(String vendorId, String manifestVendorEntry, ContentIntrospector.IntrospectionListener listener) {
        this.vendorId = vendorId;
        this.manifestVendorEntry = manifestVendorEntry;
        this.listener = listener;
    }

    @Override
    public boolean accept(String entryName, InputStream entryContentStream) throws IOException {
        if (!entryName.endsWith(".jar")) return true;
        //todo avoid the need for temporary file
        final File tempFile = copyStreamToTemporaryFile(entryName, entryContentStream);
        tempFile.deleteOnExit();
        final boolean vendorArchive = checkArchiveManifest(entryName, tempFile);
        tempFile.delete();//only for sure if the plugin is used in long live JVM
        return !vendorArchive; // we want matching vendors to be ignored
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
