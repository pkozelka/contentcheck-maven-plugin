package net.sf.buildbox.maven.contentcheck;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;

public final class SupportUtils {
   
    public static File getFile(String fileName) {
        return FileUtils.toFile(SupportUtils.class.getResource(fileName));
    }
}
