package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.model.License;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author Petr Kozelka
 */
public class LicenseShow {
    static Map<String, List<License>> parseLicenseMappingFile(File licenseMappingFile) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Object> json = objectMapper.readValue(licenseMappingFile, Map.class);
        final Map <String, List<License>> fileToLicenseMapping = new HashMap<String, List<License>>();
        final List<Map<String, Object>> jsonLicenses = (List<Map<String, Object>>) json.get("licenses");
        //construct model objects from JSON
        for (Map<String, Object> jsonLicense : jsonLicenses) {
            final License license = new License();
            license.setName((String) jsonLicense.get("name"));
            license.setUrl((String) jsonLicense.get("url"));
            final List<String> fileNames = (List<String>) jsonLicense.get("files");
            for (String fileName : fileNames) {
                fileToLicenseMapping.put(fileName, Arrays.asList(license));
            }
        }
        return fileToLicenseMapping;
    }

    static interface LicenseOutput {

        abstract void output(final Map<String, List<License>> licensesPerFile) throws IOException;

    }

    public static class CsvOutput implements LicenseOutput {

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
                    final String jarName = FileUtils.filename(entry);
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
}
