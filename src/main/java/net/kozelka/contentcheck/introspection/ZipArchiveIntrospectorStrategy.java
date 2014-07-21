package net.kozelka.contentcheck.introspection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Implementation of {@link IntrospectorInputStrategy} which can read the content of ZIP file.
 */
class ZipArchiveIntrospectorStrategy implements IntrospectorInputStrategy {
    public Set<String> readAllEntries(File containerFile) throws IOException {
        final ZipFile zipFile = new ZipFile(containerFile);
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(containerFile));
        final Set<String> entries = new HashSet<String>();
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String entryName = entry.getName();
                entries.add(entryName);
            }
        } finally {
            try {
                zis.close();
            } finally {
                zipFile.close();
            }
        }

        return entries;
    }

    public InputStream readEntryData(File containerFile, String entry) throws IOException {
        return new ZipFile(containerFile).getInputStream(new ZipEntry(entry));
    }
}
