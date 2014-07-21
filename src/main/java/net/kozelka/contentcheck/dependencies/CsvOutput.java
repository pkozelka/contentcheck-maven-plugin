package net.kozelka.contentcheck.dependencies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kozelka.contentcheck.PathUtils;
import org.apache.maven.model.License;
import org.codehaus.plexus.util.IOUtil;

public class CsvOutput implements LicenseOutput {

    private final File outputFile;

    public CsvOutput(final File outputFile) {
        super();
        this.outputFile = outputFile;
    }

    public void output(final Map<String, List<License>> entries) throws IOException{
        final Set<String> keySet = entries.keySet();
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(outputFile);
            for (String entry : keySet) {
                final List<License> licenses = entries.get(entry);
                if(licenses.size() > 1) {
                    for(License licence : licenses) {
                        csvWriter.write(String.format("%s,%s,%s%n", PathUtils.stripJARNameFromPath(entry), safeString(licence.getName()), safeString(licence.getUrl())));
                    }

                } else if(licenses.size() == 1) {
                    final License licence = licenses.get(0);
                    csvWriter.write(String.format("%s,%s,%s%n", PathUtils.stripJARNameFromPath(entry), safeString(licence.getName()), safeString(licence.getUrl())));
                } else {
                    csvWriter.write(String.format("%s,%s,%s%n", PathUtils.stripJARNameFromPath(entry), "unknown", ""));
                }
            }
            csvWriter.flush();
        } finally {
            if(csvWriter != null) {
                IOUtil.close(csvWriter);
            }
        }
    }

    public String safeString(final String s) {
        if(s == null) {
            return "";
        }
        return s.replaceAll("\\,", " ");
    }
}
