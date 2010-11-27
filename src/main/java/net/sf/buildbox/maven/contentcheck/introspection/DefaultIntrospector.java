package net.sf.buildbox.maven.contentcheck.introspection;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.maven.plugin.logging.Log;

/**
 * This introspector captures all passed entities by their paths.
 * 
 * @see #archiveEntries 
 */
public class DefaultIntrospector extends AbstractArchiveIntrospector {
    private final Set<String> archiveEntries = new LinkedHashSet<String>();

    public DefaultIntrospector(Log log, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        super(log, ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
    }

    public void processEntry(ZipEntry entry) throws IOException {
        archiveEntries.add(entry.getName());
    }

    /**
     * @return the archive's entries
     */
    public Set<String> getArchiveEntries() {
        return archiveEntries;
    }
}