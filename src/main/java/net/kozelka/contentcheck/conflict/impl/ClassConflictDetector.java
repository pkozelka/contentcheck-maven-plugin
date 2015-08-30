package net.kozelka.contentcheck.conflict.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private ArchiveInfo loadInnerArchive(ZipInputStream zis, String archiveName) throws IOException {
//        System.out.println("loadInnerArchive: " + archiveName);
        final ArchiveInfo archive = new ArchiveInfo();
        archive.setKey(archiveName);
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            if (!entry.isDirectory()) {
                processResource(archive, entry);
            }
            //
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
        return archiveInfoDao.saveArchive(archive);
    }

    private void processResource(ArchiveInfo archive, ZipEntry entry) {
        ResourceInfo resource = new ResourceInfo();
        resource.setUri(entry.getName());
        resource.setHash("crc:" + entry.getCrc());
        resource = archiveInfoDao.saveResource(resource);

        archive.addResource(resource);
        final String entryName = entry.getName();
        if (entryName.endsWith(".class")) {
            //TODO this prepares results, and will be moved to #findConflicts()
            for (ArchiveInfo hostingArchive : resource.getHostingArchives()) {
                addConflict(hostingArchive, archive, resource);
                addConflict(archive, hostingArchive, resource);
            }
        }
        resource.getHostingArchives().add(archive);
    }

    private void addConflict(ArchiveInfo thisArchive, ArchiveInfo thatArchive, ResourceInfo conflictingResource) {
        ArchiveConflict archiveConflict = new ArchiveConflict();
        archiveConflict.setThisArchive(thisArchive);
        archiveConflict.setThatArchive(thatArchive);
        archiveConflict = conflictDao.save(archiveConflict);
        archiveConflict.addResource(conflictingResource);
    }

    public List<ArchiveInfo> loadWar(File war) throws IOException {
        final List<ArchiveInfo> archives = new ArrayList<ArchiveInfo>();
        final ContentIntrospector ci = new ContentIntrospector();
        ci.setSourceFile(war);
        ci.setEntryContentFilter(new ContentIntrospector.EntryContentFilter() {
            @Override
            public boolean accept(String entryName, InputStream entryContentStream) throws IOException {
                if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
                    final ZipInputStream zis = new ZipInputStream(entryContentStream);
                    archives.add(loadInnerArchive(zis, entryName));
                }
                //TODO: add support for WEB-INF/classes as another resource
                return false;
            }
        });
        ci.walk();
        return archives;
    }
    public ConflictCheckResponse findConflicts(Collection<ArchiveInfo> archives) {
        // TODO: the conflict lookup should happen here!
        final ConflictCheckResponse response = new ConflictCheckResponse();
        response.getExploredArchives().addAll(archives);
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
        final List<ArchiveInfo> archives = ccd.loadWar(war);
        final ConflictCheckResponse response = ccd.findConflicts(archives);
        ConflictCheckResponsePrinter.printResults(response, previewThreshold, new StreamConsumer() {
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
    }
}
