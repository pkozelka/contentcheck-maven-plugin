package net.kozelka.contentcheck.conflict.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.kozelka.contentcheck.conflict.api.ArchiveInfoDao;
import net.kozelka.contentcheck.conflict.api.ConflictCheckResponse;
import net.kozelka.contentcheck.conflict.api.ConflictDao;
import net.kozelka.contentcheck.conflict.model.ArchiveConflict;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
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
    private final ConflictDao conflictDao = new ConflictDaoImpl();

    private ArchiveInfo exploreArchive(ZipInputStream zis, String archiveName) throws IOException {
//        System.out.println("exploreArchive: " + archiveName);
        final ArchiveInfo archive = new ArchiveInfo();
        archive.setKey(archiveName);
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            final String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                processClassResource(archive, entry);
            }
            //
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
        return archiveInfoDao.saveArchive(archive);
    }

    private void processClassResource(ArchiveInfo archive, ZipEntry entry) {
        ResourceInfo resource = new ResourceInfo();
        resource.setUri(entry.getName());
        resource.setHash("crc:" + entry.getCrc());
        resource = archiveInfoDao.saveResource(resource);

        archive.addResource(resource);
        //TODO following prepares the results and should be probably moved elsewhere
        for (ArchiveInfo hostingArchive : resource.getHostingArchives()) {
            addConflict(hostingArchive, archive, resource);
            addConflict(archive, hostingArchive, resource);
        }
        //
        resource.getHostingArchives().add(archive);
    }

    private void addConflict(ArchiveInfo thisArchive, ArchiveInfo thatArchive, ResourceInfo conflictingResource) {
        ArchiveConflict archiveConflict = new ArchiveConflict();
        archiveConflict.setThisArchive(thisArchive);
        archiveConflict.setThatArchive(thatArchive);
        archiveConflict = conflictDao.save(archiveConflict);
        archiveConflict.addResource(conflictingResource);
    }

    public ConflictCheckResponse exploreWar(File war) throws IOException {
        final ContentIntrospector ci = new ContentIntrospector();
        ci.setSourceFile(war);
        final ConflictCheckResponse response = new ConflictCheckResponse();
        ci.setEntryContentFilter(new ContentIntrospector.EntryContentFilter() {
            @Override
            public boolean accept(String entryName, InputStream entryContentStream) throws IOException {
                if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
                    final ZipInputStream zis = new ZipInputStream(entryContentStream);
                    ClassConflictDetector.this.exploreArchive(zis, entryName);
                    response.incrementExploredArchiveCount();
                }
                //TODO: add support for WEB-INF/classes as another resource
                return false;
            }
        });
        ci.walk();

        // fill response
        response.getArchiveConflicts().addAll(conflictDao.getAll());
        return response;
    }

    //TODO: move this to cli
    public static void main(String[] args) throws IOException {
        final int previewThreshold = 5;
        final File war = new File(args[0]);

        System.out.println("Detecting conflict in " + war);
        System.out.println("Class preview threshold: " + previewThreshold);
        final ClassConflictDetector ccd = new ClassConflictDetector();
        final ConflictCheckResponse response = ccd.exploreWar(war);
        ConflictCheckResponsePrinter.printResults(response, previewThreshold, new StreamConsumer() {
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
    }
}
