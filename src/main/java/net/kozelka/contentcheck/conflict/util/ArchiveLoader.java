package net.kozelka.contentcheck.conflict.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.model.ResourceInfo;
import net.kozelka.contentcheck.introspection.ContentIntrospector;

/**
 * Utilities helping to load archive information from the filesystem
 * @author Petr Kozelka
 */
public class ArchiveLoader {
    public static List<ArchiveInfo> loadWar(File war) throws IOException {
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

    private static ArchiveInfo loadInnerArchive(ZipInputStream zis, String archiveName) throws IOException {
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
        return archive;
    }

    private static void processResource(ArchiveInfo archive, ZipEntry entry) {
        final ResourceInfo resource = new ResourceInfo();
        resource.setUri(entry.getName());
        resource.setHash("crc:" + entry.getCrc());
        archive.addResource(resource);
    }
}
