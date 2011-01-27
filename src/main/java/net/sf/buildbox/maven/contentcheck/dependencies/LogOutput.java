package net.sf.buildbox.maven.contentcheck.dependencies;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.License;
import org.apache.maven.plugin.logging.Log;

public class LogOutput implements LicenseOutput {
    private final Log log;

    public LogOutput(final Log log) {
        super();
        this.log = log;
    }

    /**
     * @see net.sf.buildbox.maven.contentcheck.dependencies.LicenseOutput#output(java.util.Map)
     */
    public void output(final Map<String, List<License>> entries) {
        Set<String> keySet = entries.keySet();
        Set<String> knownEntries = new LinkedHashSet<String>();
        Set<String> unknownEntries = new LinkedHashSet<String>();

        for (String archiveEntry : keySet) {
            List<License> licenses = entries.get(archiveEntry);
            if(licenses.size() > 1) {
                String l = "";
                for(License licence : licenses) {
                    l+= licence.getName() + "(" +  licence.getUrl() + ") ";
                }
                knownEntries.add(String.format("%s has multiple licenses %s", archiveEntry, l));
            } else if(licenses.size() == 1) {
                License licence = licenses.get(0);
                knownEntries.add(String.format("%s %s (%s)", archiveEntry, licence.getName(), licence.getUrl()));
            } else {
                unknownEntries.add(archiveEntry);
            }
        }

        if(unknownEntries.size() == 0) {
            log.info("All artifact entries have associated license information.");
        } else {
            log.warn("Some of the entries have no associated license information or the plugin wasn't able to determine them. Please check them manually.");
        }

        log.info("");
        log.info("The archive contains following entries with known license information:");
        for (String entryDesc : knownEntries) {
            log.info(entryDesc);
        }

        if(unknownEntries.size() > 0) {
            log.info("");
            log.warn("The archive contains following entries with uknown license inforamtion:");
            for (String archiveName : unknownEntries) {
                log.warn(archiveName);
            }
        }
    }
}
