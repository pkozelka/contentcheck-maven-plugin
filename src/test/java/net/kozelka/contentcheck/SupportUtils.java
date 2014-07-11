package net.kozelka.contentcheck;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;

public final class SupportUtils {
    public final static String DEFAULT_VENDOR_MANIFEST_ENTRY_NAME = "Implementation-Vendor-Id";
    public final static String DEFAULT_CHECK_FILES_PATTERN = "**/*.jar";
   
    public static File getFile(String fileName) {
        return FileUtils.toFile(SupportUtils.class.getResource(fileName));
    }
}
