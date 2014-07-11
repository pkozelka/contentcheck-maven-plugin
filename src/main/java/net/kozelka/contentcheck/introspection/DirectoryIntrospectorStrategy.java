package net.kozelka.contentcheck.introspection;

import org.apache.commons.lang.Validate;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link IntrospectorInputStrategy} which can read the content of ordinary directory.
 */
public class DirectoryIntrospectorStrategy implements IntrospectorInputStrategy {

    public Set<String> readAllEntries(File baseDirectory) throws IOException {
        Validate.notNull(baseDirectory, "containerFile cannot be null!");
        Validate.isTrue(baseDirectory.isDirectory(), baseDirectory.getAbsolutePath() + " is not a directory!");
        final Set<String> entries = new HashSet<String>();
        entries.addAll(FileUtils.getFileNames(baseDirectory, null, null, false));
        final List<String> directories = FileUtils.getDirectoryNames(baseDirectory, null, null, false);
        // strangely, the empty string is returned in result, we have to remove it explicitly.
        directories.remove("");
        // directories does not have trailing slash, so we need to add it
        for (String directory : directories) {
            entries.add(directory + "/");
        }
        return entries;
    }

    public InputStream readEntryData(File containerFile, String entry) throws IOException {
        return new FileInputStream(new File(containerFile, entry));
    }

}
