package net.sf.buildbox.contentcheck.conflict;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
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
        while (entry != null) {
            final String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                processClassResource(archiveInfo, entry);
            }
            //
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
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

    public static ClassConflictDetector exploreWar(File war) throws IOException {
        final ClassConflictDetector ccd = new ClassConflictDetector();
        //TODO: iterate over all inner jars
        final ZipInputStream waris = new ZipInputStream(new FileInputStream(war));
        ZipEntry entry = waris.getNextEntry();
        while (entry != null) {
            final String entryName = entry.getName();
            if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
                final ZipInputStream zis = new ZipInputStream(waris);
                ccd.exploreArchive(zis, entry.getName());
            }
            waris.closeEntry();
            entry = waris.getNextEntry();
        }
        return ccd;
    }

    public static void main(String[] args) throws IOException {
        final int previewThreshold = 5;
        final File war = new File(args[0]);

        System.out.println("Detecting conflict in " + war);
        System.out.println("Class preview threshold: " + previewThreshold);
        final ClassConflictDetector ccd = ClassConflictDetector.exploreWar(war);
        final List<ArchiveInfo> sortedConflictingArchives = new ArrayList<ArchiveInfo>(ccd.getConflictingArchives());
        Collections.sort(sortedConflictingArchives, new Comparator<ArchiveInfo>() {
            public int compare(ArchiveInfo o1, ArchiveInfo o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        int totalConflicts = 0;
        for (ArchiveInfo cai : sortedConflictingArchives) {
            System.out.println(String.format("\nFile '%s' has", cai.getKey()));
            final List<Conflict> sortedConflicts = new ArrayList<Conflict>(cai.getConflicts());
            Collections.sort(sortedConflicts, new Comparator<Conflict>() {
                public int compare(Conflict o1, Conflict o2) {
                    return o1.otherArchive.getKey().compareTo(o2.otherArchive.getKey());
                }
            });
            for (Conflict conflict : sortedConflicts) {
                totalConflicts += conflict.resources.size();
                System.out.println(String.format("%8d class conflict with '%s'", conflict.resources.size(), conflict.otherArchive.getKey()));
                if (conflict.resources.size() < previewThreshold) {
                    for (ResourceInfo resource : conflict.resources) {
                        System.out.println(String.format("                %s", resource.key));
                    }
                }
            }
        }
        System.out.println("-------------------------------------------------");
        System.out.println(String.format("Total: %d conflict affects %d of %d archives.",
                totalConflicts,
                sortedConflictingArchives.size(),
                ccd.getExploredArchives().size()));
    }
}
