package net.kozelka.contentcheck.expect.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.kozelka.contentcheck.expect.model.ActualEntry;

public final class ExpectUtils {
    private ExpectUtils() {}

    public static void generateListing(Collection<ActualEntry> actualEntries, File generatedFile) throws IOException {
        final List<ActualEntry> sortedEntries = new ArrayList<ActualEntry>(actualEntries);
        Collections.sort(sortedEntries, new Comparator<ActualEntry>() {
            public int compare(ActualEntry o1, ActualEntry o2) {
                return o1.getUri().compareTo(o2.getUri());
            }
        });
        generatedFile.getParentFile().mkdirs();
        final FileWriter writer = new FileWriter(generatedFile);
        try {
            writer.write(String.format("#%n# Edit this file to approve or unpraprove individual libraries; will be checked by contentcheck-maven-plugin.%n#%n"));
            writer.write(String.format("# Keep the entries sorted alphabetically for easier eye-seeking.%n#%n"));
            for (final ActualEntry actualEntry : sortedEntries) {
                writer.write(String.format("%s%n", actualEntry.getUri()));
            }
        } finally {
            writer.close();
        }
    }
}
