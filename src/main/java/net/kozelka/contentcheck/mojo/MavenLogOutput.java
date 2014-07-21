package net.kozelka.contentcheck.mojo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kozelka.contentcheck.dependencies.LicenseOutput;
import org.apache.maven.model.License;
import org.apache.maven.plugin.logging.Log;

class MavenLogOutput implements LicenseOutput {
    private final Log log;

    public MavenLogOutput(final Log log) {
        super();
        this.log = log;
    }

    /**
     * @see LicenseOutput#output(java.util.Map)
     */
    public void output(final Map<String, List<License>> licensesPerFile) {
        final Set<String> keySet = licensesPerFile.keySet();
        final Set<String> knownEntries = new LinkedHashSet<String>();
        final Set<String> unknownEntries = new LinkedHashSet<String>();

        for (String entry : keySet) {
            final List<License> licenses = licensesPerFile.get(entry);
            if(licenses.size() > 1) {
                final StringBuilder sb = new StringBuilder("");
                for(License licence : licenses) {
                    sb.append(String.format("%s(%s) ", licence.getName(), licence.getUrl()));
                }
                knownEntries.add(String.format("%s has multiple licenses: %s", entry, sb));
            } else if(licenses.size() == 1) {
                final License licence = licenses.get(0);
                knownEntries.add(String.format("%s %s (%s)", entry, licence.getName(), licence.getUrl()));
            } else {
                unknownEntries.add(entry);
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
