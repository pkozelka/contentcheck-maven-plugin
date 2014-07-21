package net.kozelka.contentcheck.dependencies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kozelka.contentcheck.PathUtils;
import org.apache.maven.model.License;

public class CsvOutput implements LicenseOutput {

    private final File outputFile;

    public CsvOutput(final File outputFile) {
        super();
        this.outputFile = outputFile;
    }

    public void output(final Map<String, List<License>> licensesPerFile) throws IOException {
        final Set<String> keySet = licensesPerFile.keySet();
        final FileWriter csvWriter = new FileWriter(outputFile);
        try {
            for (String entry : keySet) {
                final List<License> licenses = licensesPerFile.get(entry);
                final String jarName = PathUtils.stripJARNameFromPath(entry);
                for(License licence : licenses) {
                    writeRecord(csvWriter, jarName, licence.getName(), licence.getUrl());
                }
                if(licenses.isEmpty()) {
                    writeRecord(csvWriter, jarName, "unknown", "");
                }
            }
        } finally {
            csvWriter.close();
        }
    }

    private void writeRecord(FileWriter csvWriter, String jarName, String licenseName, String licenseUrl) throws IOException {
        csvWriter.write(String.format("%s,%s,%s%n", jarName, safeString(licenseName), safeString(licenseUrl)));
    }

    public String safeString(final String s) {
        if(s == null) {
            return "";
        }
        return s.replaceAll("\\,", " ");
    }
}
