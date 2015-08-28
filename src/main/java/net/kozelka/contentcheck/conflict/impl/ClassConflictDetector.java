package net.kozelka.contentcheck.conflict.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.kozelka.contentcheck.conflict.api.ArchiveInfoDao;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ConflictingArchive;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @todo improve design
 * @todo write tests
 * @author Petr Kozelka
 */
public class ClassConflictDetector {
    private final ArchiveInfoDao archiveInfoDao = new ArchiveInfoDaoImpl();

    private ArchiveInfo exploreArchive(ZipInputStream zis, String archiveName) throws IOException {
//        System.out.println("exploreArchive: " + archiveName);
        final ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setKey(archiveName);
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
        archiveInfoDao.saveArchive(archiveInfo);
        return archiveInfo;
    }

    private void processClassResource(ArchiveInfo archiveInfo, ZipEntry entry) {
        final String key = entry.getName();
//        System.out.println(" : " + key);
        ResourceInfo resourceInfo = archiveInfoDao.findResourceByKey(key);
        if (resourceInfo == null) {
            resourceInfo = new ResourceInfo();
            resourceInfo.setUri(key);
            resourceInfo.setHash("crc:" + entry.getCrc());
            resourceInfo = archiveInfoDao.saveResource(resourceInfo);
        }
        addHostingArchive(archiveInfo, resourceInfo);
    }

    private void addHostingArchive(ArchiveInfo archiveInfo, ResourceInfo resourceInfo) {
        for (ArchiveInfo hostingArchive : resourceInfo.getHostingArchives()) {
            addConflict(hostingArchive, archiveInfo, resourceInfo);
            addConflict(archiveInfo, hostingArchive, resourceInfo);
        }
        resourceInfo.getHostingArchives().add(archiveInfo);
    }

    private void addConflict(ArchiveInfo thisArchive, ArchiveInfo thatArchive, ResourceInfo conflictingResource) {
        final String conflictingArchiveKey = thatArchive.getKey();
        ConflictingArchive conflictingArchive = findConflictingArchiveByKey(thisArchive, conflictingArchiveKey);
        if (conflictingArchive == null) {
            conflictingArchive = new ConflictingArchive();
            conflictingArchive.setConflictingArchive(thatArchive);
            thisArchive.getConflictingArchives().add(conflictingArchive);
        }
        conflictingArchive.addResource(conflictingResource);
    }

    private static ConflictingArchive findConflictingArchiveByKey(ArchiveInfo thisArchive, String key) {
        for (ConflictingArchive conflictingArchive : thisArchive.getConflictingArchives()) {
            if (key.equals(conflictingArchive.getArchiveInfo().getKey())) {
                return conflictingArchive;
            }
        }
        return null;
    }

    public Set<ArchiveInfo> getExploredArchives() {
        return archiveInfoDao.getAllArchives();
    }

    public List<ArchiveInfo> getConflictingArchives() {
        final List<ArchiveInfo> conflictingArchives = new ArrayList<ArchiveInfo>();
        for (ArchiveInfo archive : archiveInfoDao.getAllArchives()) {
            if (! archive.getConflictingArchives().isEmpty()) {
                conflictingArchives.add(archive);
            }
        }
        return conflictingArchives;
    }

    public void exploreWar(File war) throws IOException {
        final ContentIntrospector ci = new ContentIntrospector();
        ci.setSourceFile(war);
        ci.setEntryContentFilter(new ContentIntrospector.EntryContentFilter() {
            @Override
            public boolean accept(String entryName, InputStream entryContentStream) throws IOException {
                if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
                    final ZipInputStream zis = new ZipInputStream(entryContentStream);
                    ClassConflictDetector.this.exploreArchive(zis, entryName);
                }
                //TODO: add support for WEB-INF/classes as another resource
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
            final List<ConflictingArchive> sortedConflicts = new ArrayList<ConflictingArchive>(cai.getConflictingArchives());
            Collections.sort(sortedConflicts, new Comparator<ConflictingArchive>() {
                public int compare(ConflictingArchive o1, ConflictingArchive o2) {
                    return o1.getArchiveInfo().getKey().compareTo(o2.getArchiveInfo().getKey());
                }
            });
            for (ConflictingArchive conflictingArchive : sortedConflicts) {
                final List<ResourceInfo> conflictResources = conflictingArchive.getResources();
                final int conflictResourceCount = conflictResources.size();
                totalConflicts += conflictResourceCount;
                output.consumeLine(String.format("%8d class conflicts with '%s'", conflictResourceCount, conflictingArchive.getArchiveInfo().getKey()));
                if (previewThreshold == 0) continue;
                int cnt = 0;
                for (ResourceInfo resource : conflictResources) {
                    cnt ++;
                    if (cnt > previewThreshold && previewThreshold >= 0) {
                        output.consumeLine("                ...");
                        break;
                    }
                    output.consumeLine(String.format("                %s", resource.getUri()));
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
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
    }
}
