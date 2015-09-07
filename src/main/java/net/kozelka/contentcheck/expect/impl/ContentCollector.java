package net.kozelka.contentcheck.expect.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.introspection.ContentIntrospector;

/**
 * This listener collects actual entries from an archive.
 */
public class ContentCollector implements ContentIntrospector.Events {
    private final Collection<ActualEntry> actualEntries;

    public ContentCollector(Collection<ActualEntry> actualEntries) {
        this.actualEntries = actualEntries;
    }

    public void readingSourceFile(File sourceFile) {
    }

    public void skippingEntryNotMatching(String entry) {
    }

    public void skippingEntryOwnModule(String entry) {
    }

    public void cannotCheckManifest(String jarPath, Exception e) {
    }

    public void cannotClose(String jarPath, IOException e) {
    }

    public void checkingInTmpfile(String jarPath, File tempFile) {
    }

    public void processEntry(String entryName) {
        final ActualEntry actualEntry = new ActualEntry();
        actualEntry.setUri(entryName);
        actualEntries.add(actualEntry);
    }
}
