package net.sf.buildbox.contentcheck.introspection;

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This introspector captures all passed entities by their paths.
 * 
 * @see #sourceEntries
 */
public class DefaultIntrospector extends AbstractIntrospector {
    private final Set<String> sourceEntries = new LinkedHashSet<String>();

    public DefaultIntrospector(Log log, boolean ignoreVendorArchives, String vendorId, String manifestVendorEntry, String checkFilesPattern) {
        super(log, ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
    }

    public void processEntry(String entry) throws IOException {
        sourceEntries.add(entry);
    }

    /**
     * @return the entries found in source
     */
    public Set<String> getEntries() {
        return sourceEntries;
    }
}