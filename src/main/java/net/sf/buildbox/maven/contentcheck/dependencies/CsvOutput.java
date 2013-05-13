package net.sf.buildbox.maven.contentcheck.dependencies;

import static net.sf.buildbox.maven.contentcheck.PathUtils.stripJARNameFromPath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.License;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;

public class CsvOutput implements LicenseOutput {

    private final File outputFile;
    private final Log log;

    public CsvOutput(final Log log, final File outputFile) {
        super();
        this.outputFile = outputFile;
        this.log = log;
    }

    /**
     * @see net.sf.buildbox.maven.contentcheck.dependencies.LicenseOutput#output(java.util.Map)
     */
    public void output(final Map<String, List<License>> entries) throws IOException{
        Set<String> keySet = entries.keySet();
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(outputFile);
            log.info(String.format("Creating license output to CSV file %s", outputFile.getPath()));
            for (String entry : keySet) {
                List<License> licenses = entries.get(entry);
                if(licenses.size() > 1) {
                    for(License licence : licenses) {
                        csvWriter.write(String.format("%s,%s,%s\n", stripJARNameFromPath(entry), safeString(licence.getName()), safeString(licence.getUrl())));
                    }

                } else if(licenses.size() == 1) {
                    License licence = licenses.get(0);
                    csvWriter.write(String.format("%s,%s,%s\n", stripJARNameFromPath(entry), safeString(licence.getName()), safeString(licence.getUrl())));
                } else {
                    csvWriter.write(String.format("%s,%s,%s\n", stripJARNameFromPath(entry), "unknown", ""));
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
