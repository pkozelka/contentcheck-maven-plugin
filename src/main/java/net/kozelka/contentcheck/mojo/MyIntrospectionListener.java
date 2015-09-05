package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.apache.maven.plugin.logging.Log;

/**
* @author Petr Kozelka
*/
public class MyIntrospectionListener implements ContentIntrospector.Events {
    private final Log log;

    public //TODO: just for now (#10)
    MyIntrospectionListener(Log log) {
        this.log = log;
    }

    public void readingSourceFile(File sourceFile) {
        log.info("Reading source file: " + sourceFile);
    }

    public void skippingEntryNotMatching(String entry) {
        log.debug(String.format("Skipping '%s' (not matching)", entry));
    }

    public void skippingEntryOwnModule(String entry) {
        log.info(String.format("Skipping '%s' (vendor archive)", entry));
    }

    public void cannotCheckManifest(String jarPath, Exception e) {
        log.warn("Cannot check MANIFEST.MF file in JAR archive " + jarPath, e);
    }

    public void cannotClose(String jarPath, IOException e) {
        log.warn("Cannot close temporary JAR file " + jarPath,e);
    }

    public void checkingInTmpfile(String jarPath, File tempFile) {
        log.debug("Checking " + jarPath + " to be a vendor archive, using tempfile " + tempFile);
    }

    public void processEntry(String entryName) {
        log.debug("Found: " + entryName);
    }
}
