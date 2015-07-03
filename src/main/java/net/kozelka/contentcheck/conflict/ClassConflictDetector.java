package net.kozelka.contentcheck.conflict;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @todo improve design
 * @todo write tests
 * @author Petr Kozelka
 */
public class ClassConflictDetector {
    private final Set<ArchiveInfo> exploredArchives = new LinkedHashSet<ArchiveInfo>();
    private final Map<String, ResourceInfo> exploredResources = new LinkedHashMap<String, ResourceInfo>();

    private ArchiveInfo exploreArchive(ZipInputStream zis, String archiveName) throws IOException {
//        System.out.println("exploreArchive: " + archiveName);
        final ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setKey(archiveName);
        exploredArchives.add(archiveInfo);
        ZipEntry entry = zis.getNextEntry();
        int classCount = 0;
        while (entry != null) {
            final String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                processClassResource(archiveInfo, entry);
                classCount ++;
            }
            //
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
        archiveInfo.setClassCount(classCount);
        return archiveInfo;
    }

    private void processClassResource(ArchiveInfo archiveInfo, ZipEntry entry) {
        final String key = entry.getName();
//        System.out.println(" : " + key);
        ResourceInfo resourceInfo = exploredResources.get(key);
        if (resourceInfo == null) {
            resourceInfo = new ResourceInfo();
            resourceInfo.key = key;
            exploredResources.put(key, resourceInfo);
        }
        resourceInfo.addHostingArchive(archiveInfo);
    }

    public Set<ArchiveInfo> getExploredArchives() {
        return exploredArchives;
    }

    public List<ArchiveInfo> getConflictingArchives() {
        final List<ArchiveInfo> conflictingArchives = new ArrayList<ArchiveInfo>();
        for (ArchiveInfo archive : exploredArchives) {
            if (! archive.getConflicts().isEmpty()) {
                conflictingArchives.add(archive);
            }
        }
        return conflictingArchives;
    }

    public void exploreWar(File war) throws IOException {
        final ContentIntrospector ci = new ContentIntrospector();
        ci.setSourceFile(war);
        ci.setEntryContentFilter(new ContentIntrospector.EntryContentFilter() {
            public boolean accept(String entryName, InputStream entryContentStream) throws IOException {
                if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
                    final ZipInputStream zis = new ZipInputStream(entryContentStream);
                    ClassConflictDetector.this.exploreArchive(zis, entryName);
                }
                return false;
            }
        });
        ci.walk();
    }

    public int printResults(int previewThreshold, StreamConsumer output) {
        final List<ArchiveInfo> sortedConflictingArchives = new ArrayList<ArchiveInfo>(getConflictingArchives());
        Collections.sort(sortedConflictingArchives, new Comparator<ArchiveInfo>() {
            public int compare(ArchiveInfo o1, ArchiveInfo o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        int totalConflicts = 0;
        for (ArchiveInfo cai : sortedConflictingArchives) {
            output.consumeLine("");
            output.consumeLine(String.format("File '%s' (%d classes):", cai.getKey(), cai.getClassCount()));
            final List<Conflict> sortedConflicts = new ArrayList<Conflict>(cai.getConflicts());
            Collections.sort(sortedConflicts, new Comparator<Conflict>() {
                public int compare(Conflict o1, Conflict o2) {
                    return o1.getOtherArchive().getKey().compareTo(o2.getOtherArchive().getKey());
                }
            });
            for (Conflict conflict : sortedConflicts) {
                final List<ResourceInfo> conflictResources = conflict.getResources();
                final int conflictResourceCount = conflictResources.size();
                totalConflicts += conflictResourceCount;
                output.consumeLine(String.format("%8d class conflicts with '%s'", conflictResourceCount, conflict.getOtherArchive().getKey()));
                if (previewThreshold == 0) continue;
                int cnt = 0;
                for (ResourceInfo resource : conflictResources) {
                    cnt ++;
                    if (cnt > previewThreshold && previewThreshold >= 0) {
                        output.consumeLine("                ...");
                        break;
                    }
                    output.consumeLine(String.format("                %s", resource.key));
                }
            }
        }
        output.consumeLine("-------------------------------------------------");
        output.consumeLine(String.format("Total: %d conflicts affect %d of %d archives.",
                totalConflicts,
                sortedConflictingArchives.size(),
                getExploredArchives().size()));
        return totalConflicts;
    }

    //TODO: move this to cli
    public static void main(String[] args) throws IOException {
        final int previewThreshold = 5;
        final File war = new File(args[0]);

        System.out.println("Detecting conflict in " + war);
        System.out.println("Class preview threshold: " + previewThreshold);
        final ClassConflictDetector ccd = new ClassConflictDetector();
        ccd.exploreWar(war);
        ccd.printResults(previewThreshold, new StreamConsumer() {
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
    }
}
