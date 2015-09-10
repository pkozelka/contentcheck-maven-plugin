package net.kozelka.contentcheck.expect.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import net.kozelka.contentcheck.expect.model.ActualEntry;

public final class ExpectUtils {
    private ExpectUtils() {}

    public static void generateListing(List<ActualEntry> actualEntries, File generatedFile) throws IOException {
        generatedFile.getParentFile().mkdirs();
        final FileWriter writer = new FileWriter(generatedFile);
        try {
            writer.write(String.format("#%n# Edit this file to approve or unpraprove individual libraries; will be checked by contentcheck-maven-plugin.%n#%n"));
            writer.write(String.format("#%n# Keep the entries sorted alphabetically for easier eye-seeking.%n#%n"));
            for (final ActualEntry actualEntry : actualEntries) {
                writer.write(String.format("%s%n", actualEntry));
            }
        } finally {
            writer.close();
        }
    }
}
