package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.IOException;
import net.kozelka.contentcheck.introspection.IntrospectionListener;
import org.apache.maven.plugin.logging.Log;

/**
* @author Petr Kozelka
*/
public class MyIntrospectionListener implements IntrospectionListener {
    private final Log log;

    public //TODO: just for now (#10)
    MyIntrospectionListener(Log log) {
        this.log = log;
    }

    @Override public void readingSourceFile(File sourceFile) {
        log.info("Reading source file: " + sourceFile);
    }

    @Override public void skippingEntry(String entry) {
        log.debug(String.format("Skipping entry '%s' - doesn't match the required pattern", entry));
    }

    @Override public void skippingEntryOwnModule(String entry) {
        log.debug(String.format("Skipping entry '%s' it's a vendor archive", entry));
    }

    @Override public void cannotCheckManifest(String jarPath, Exception e) {
        log.warn("Cannot check MANIFEST.MF file in JAR archive " + jarPath, e);
    }

    @Override public void cannotClose(String jarPath, IOException e) {
        log.warn("Cannot close temporary JAR file " + jarPath,e);
    }

    @Override public void checkingInTmpfile(String jarPath, File tempFile) {
        log.debug("Checking " + jarPath + " to be a vendor archive, using tempfile " + tempFile);
    }
}
