package net.kozelka.contentcheck;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;

public final class SupportUtils {
    public static final String VENDOR1 = "com.buildbox"; //TODO: change to "com.vendor1" + the same in test resource binaries; these should be created by code (ant or java)
    public final static String DEFAULT_CHECK_FILES_PATTERN = "**/*.jar";

    public static File getFile(String fileName) {
        return FileUtils.toFile(SupportUtils.class.getResource(fileName));
    }
}
