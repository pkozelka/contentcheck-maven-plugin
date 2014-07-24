package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.IOException;

/**
 * @author Petr Kozelka
 */
public interface IntrospectionListener {
    void readingSourceFile(File sourceFile);

    void skippingEntryNotMatching(String entry);

    void skippingEntryOwnModule(String entry);

    void cannotCheckManifest(String jarPath, Exception e);

    void cannotClose(String jarPath, IOException e);

    void checkingInTmpfile(String jarPath, File tempFile);
}
