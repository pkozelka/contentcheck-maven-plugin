package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import net.kozelka.contentcheck.util.EventSink;
import org.codehaus.plexus.util.IOUtil;

/**
 * Determines if an entry is matching given vendor.
 *
 * @author Petr Kozelka
 */
public class VendorFilter implements ContentIntrospector.EntryContentFilter {

    public final static String DEFAULT_VENDOR_MANIFEST_ENTRY_NAME = "Implementation-Vendor-Id";

    private final String vendorId;
    private String manifestVendorEntry = DEFAULT_VENDOR_MANIFEST_ENTRY_NAME;
    //todo: use own events!
    private EventSink<ContentIntrospector.Events> events = EventSink.create(ContentIntrospector.Events.class);

    public VendorFilter(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setManifestVendorEntry(String manifestVendorEntry) {
        this.manifestVendorEntry = manifestVendorEntry;
    }

    public EventSink<ContentIntrospector.Events> getEvents() {
        return events;
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
            events.fire.cannotCheckManifest(jarPath, e);
        } finally {
            if(jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    events.fire.cannotClose(jarPath, e);
                }
            }
        }
        return false;
    }

    private File copyStreamToTemporaryFile(final String jarPath, final InputStream archiveInputStream) throws IOException {
        final File tempFile = File.createTempFile(UUID.randomUUID().toString(), "jar");
        final FileOutputStream fos = new  FileOutputStream(tempFile);
        try {
            events.fire.checkingInTmpfile(jarPath, tempFile);
            IOUtil.copy(archiveInputStream, fos);
            return tempFile;
        } finally {
            fos.close();
        }
    }
}
