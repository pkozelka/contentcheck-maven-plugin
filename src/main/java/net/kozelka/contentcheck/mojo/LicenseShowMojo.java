package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kozelka.contentcheck.introspection.ContentIntrospector;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.report.projectinfo.dependencies.Dependencies;
import org.apache.maven.report.projectinfo.dependencies.RepositoryUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.plexus.util.FileUtils;

/**
 * Shows license information for selected source entries. By default, the information is parsed from dependency's POM,
 * but the project may define additional mapping between files in output archive and licenses.
 */
@Mojo(name = "show-licenses")
public class LicenseShowMojo extends AbstractArchiveContentMojo{
    /**
     * The license mapping file, in JSON format.
     * This file may define additional license information for JARs that are not recognized.
     * <h4>Additional license information</h4>
     * <pre><code>{
     * "licenses": [
     *       {
     *          "name" : "License name",
     *           "url"  : "License text URL",
     *           "files": [
     *               "file name"
     *            ]
     *       }
     *  ]
     * }</code></pre>
     * <h4>Example</h4>
     * <pre><code>{
     *    "licenses": [
     *       {
     *           "name"  : "The LGPL license 2.1",
     *           "url"   : "http://www.gnu.org/licenses/lgpl-2.1.html",
     *           "files" : [
     *                         "aspectwerkz-nodeps-jdk5-2.2.1.jar"
     *           ]
     *       },
     *       {
     *           "name"  : "The public domain",
     *           "url"   : "http://creativecommons.org/licenses/publicdomain/",
     *           "files" : [
     *               "jsr166x-1.0.jar",
     *               "xyz.jar"
     *           ]
     *       }
     *    ]
     * }</code></pre>
     */
    @Parameter(defaultValue = "src/main/license.mapping.json", property = "licenseMappingFile")
    File licenseMappingFile;


    /**
     * If true print the result of check to a CSV file in project build directory.
     * See {@link #csvOutputFile}
     */
    @Parameter(defaultValue = "true")
    boolean csvOutput;

    /**
     * The CSV output file that is used when {@link #csvOutput} is turned on.
     */
    @Parameter(defaultValue = "${project.build.directory}/licenses.csv")
    File csvOutputFile;

    /**
     * The archive file to be checked
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
    File defaultBundleForPOMPacking;

    /**
     * Artifact collector component.
     */
    @Component
    ArtifactCollector collector;

    /**
     * Artifact Factory component.
     */
    @Component
    ArtifactFactory factory;

    /**
     * Dependency tree builder component.
     */
    @Component
    DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * Artifact metadata source component.
     */
    @Component
    ArtifactMetadataSource artifactMetadataSource;

    /**
     * Jar classes analyzer component.
     */
    @Component
    JarClassesAnalysis classesAnalyzer;

    /**
     * Local Repository.
     */
    @Parameter(property = "localRepository", required = true, readonly = true)
    ArtifactRepository localRepository;

    /**
     * Maven Project Builder component.
     */
    @Component
    MavenProjectBuilder mavenProjectBuilder;

    /**
     * The current user system settings for use in Maven.
     */
    @Parameter(property = "settings", required = true, readonly = true)
    protected Settings settings;

    /**
     * The Maven Project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

    /**
     * Wagon manager component.
     */
    @Component
    WagonManager wagonManager;

    /**
     * Artifact Resolver component.
     */
    @Component
    protected ArtifactResolver resolver;

    /**
     * Repository metadata component.
     */
    @Component
    RepositoryMetadataManager repositoryMetadataManager;

    /**
     * Artifact resolving and the rest of Maven repo magic taken from <a href="http://maven.apache.org/plugins/maven-project-info-reports-plugin/index.html">Maven Project Info Reports Plugin</a>.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File src = sourceFile.exists() ? sourceFile : defaultBundleForPOMPacking;
        if(!src.exists()) {
            getLog().warn("Skipping project since there is no archive to check.");
            return;
        }

        final List<MavenProject> mavenProjectForDependencies = getMavenProjectForDependencies();

        try {
            final ContentIntrospector introspector = ContentIntrospector.create(new MyIntrospectionListener(getLog()),
                    ignoreVendorArchives, vendorId, manifestVendorEntry, checkFilesPattern);
            introspector.readEntries(src);

            final Set<String> archiveEntries = new LinkedHashSet<String>(introspector.getEntries());
            final Map<String, List<License>> entries = new LinkedHashMap<String, List<License>>();

            final Map<String, List<License>> additionalLicenseInformation = new LinkedHashMap<String, List<License>>();
            if(licenseMappingFile != null && licenseMappingFile.exists()) {
                //read additional license information
                getLog().info(String.format("Reading license mapping file %s", licenseMappingFile.getAbsolutePath()));
                additionalLicenseInformation.putAll(parseLicenseMappingFile(licenseMappingFile));
            }

            getLog().info("Comparing the archive content with Maven project artifacts");
            for(String archiveEntry : archiveEntries) {
                List<License> licenses = null; //these licenses will be associated with the given archive entry

                for (MavenProject mavenProject : mavenProjectForDependencies) {
                    mavenProject.getGroupId();
                    final String artifactId = mavenProject.getArtifactId();
                    final String version = mavenProject.getVersion();
                    final String jarName = artifactId + "-" + version + ".jar"; //guess jar name
                    if(archiveEntry.endsWith(jarName)) {
                        @SuppressWarnings("unchecked")
                        final List<License> _licenses = mavenProject.getLicenses();
                        licenses = _licenses == null || _licenses.size() == 0 ? null : _licenses  ;
                        break;
                    }
                }

                final List<License> licensesMappingFile = additionalLicenseInformation.get(FileUtils.filename(archiveEntry));

                if(licenses == null && licensesMappingFile == null) {//misising license information
                    getLog().debug(String.format("Cannot resolve license information for archive entry %s neither from the POM file nor the file for license mapping", archiveEntry));
                    //archive entry must be present even if there is no a matching Maven Project
                    entries.put(archiveEntry, Collections.<License>emptyList());
                } else if(licenses != null && licensesMappingFile != null) {//licenses specified in both - POM and license mapping file
                    getLog().warn(String.format("The license information for file %s are defined in the POM file and also in the file for license mapping. Using license information from the the file for license mapping.", archiveEntry));
                    entries.put(archiveEntry, licensesMappingFile); //mapping manually specified licenses precedes licenses from POM
                } else if (licenses != null) {//license information in POM
                    entries.put(archiveEntry, licenses);//license
                } else {
                    //license information in mapping file
                    //put additional license information to the object that holds this information
                    entries.put(archiveEntry, licensesMappingFile);
                }
            }

            final LicenseOutput logOutput = new MavenLogOutput(getLog());
            logOutput.output(entries);

            if(csvOutput) {
                final CsvOutput csvOutput = new CsvOutput(csvOutputFile);
                getLog().info(String.format("Creating license output to CSV file %s", csvOutputFile.getPath()));
                csvOutput.output(entries);
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    static Map<String, List<License>> parseLicenseMappingFile(File licenseMappingFile) throws MojoFailureException{
        final ObjectMapper objectMapper = new ObjectMapper();
        final String licenseMappingFilePath = licenseMappingFile.getPath();
        final Map<String, Object> json;
        try {
            json= objectMapper.readValue(licenseMappingFile, Map.class);
        } catch (JsonParseException e) {
            throw new MojoFailureException(String.format("Cannot parse JSON from file %s the content of the file is not well formed JSON.", licenseMappingFilePath),e);
        } catch (JsonMappingException e) {
            throw new MojoFailureException(String.format("Cannot deserialize JSON from file %s", licenseMappingFilePath),e);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
        final Map <String, List<License>> fileToLicenseMapping = new HashMap<String, List<License>>();
        final List<Map<String, Object>> jsonLicenses = (List<Map<String, Object>>) json.get("licenses");
        //construct model objects from JSON
        for (Map<String, Object> jsonLicense : jsonLicenses) {
            final License license = new License();
            license.setName((String) jsonLicense.get("name"));
            license.setUrl((String) jsonLicense.get("url"));
            final List<String> fileNames = (List<String>) jsonLicense.get("files");
            for (String fileName : fileNames) {
                fileToLicenseMapping.put(fileName, Arrays.asList(license));
            }
        }
        return fileToLicenseMapping;
    }

    private List<MavenProject> getMavenProjectForDependencies() throws MojoExecutionException, MojoFailureException {
        final DependencyNode dependencyTreeNode = resolveProject();
        final Dependencies dependencies = new Dependencies( project, dependencyTreeNode, classesAnalyzer );
        final Log log = getLog();
        final RepositoryUtils repoUtils = new RepositoryUtils( log, wagonManager, settings, mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(), project.getPluginArtifactRepositories(), localRepository,repositoryMetadataManager );
        final Artifact projectArtifact = project.getArtifact();
        log.info(String.format("Resolving project %s:%s:%s dependencies", projectArtifact.getGroupId(), projectArtifact.getArtifactId(), projectArtifact.getVersion()));
        final List<Artifact> allDependencies = dependencies.getAllDependencies();
        final List<MavenProject> mavenProjects = new ArrayList<MavenProject>();
        for (Artifact artifact : allDependencies) {
            log.debug(String.format("Resolving project information for %s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
            try {
                final MavenProject mavenProject = repoUtils.getMavenProjectFromRepository(artifact);
                mavenProjects.add(mavenProject);
            } catch (ProjectBuildingException e) {
                throw new MojoFailureException(String.format("Cannot get project information for artifact %s:%s:%s from repository",artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()), e);
            }
        }
        return mavenProjects;
    }

    /**
     * @return resolve the dependency tree
     */
    private DependencyNode resolveProject()
    {
        try
        {
            final ArtifactFilter artifactFilter = new ScopeArtifactFilter( Artifact.SCOPE_TEST );
            return dependencyTreeBuilder.buildDependencyTree( project, localRepository, factory, artifactMetadataSource, artifactFilter, collector );
        }
        catch ( DependencyTreeBuilderException e )
        {
            getLog().error( "Unable to build dependency tree.", e );
            return null;
        }
    }

    /**
     * MavenProject placeholder for JARs that we are not able to connect with
     * existing MavenProjects.
     */
    public static class NullMavenProject extends MavenProject {
        @Override
        public List getLicenses() {
            return Collections.EMPTY_LIST;
        }

    }

    public static class CsvOutput implements LicenseOutput {

        private final File outputFile;

        public CsvOutput(final File outputFile) {
            super();
            this.outputFile = outputFile;
        }

        public void output(final Map<String, List<License>> licensesPerFile) throws IOException {
            final Set<String> keySet = licensesPerFile.keySet();
            final FileWriter csvWriter = new FileWriter(outputFile);
            try {
                for (String entry : keySet) {
                    final List<License> licenses = licensesPerFile.get(entry);
                    final String jarName = FileUtils.filename(entry);
                    for(License licence : licenses) {
                        writeRecord(csvWriter, jarName, licence.getName(), licence.getUrl());
                    }
                    if(licenses.isEmpty()) {
                        writeRecord(csvWriter, jarName, "unknown", "");
                    }
                }
            } finally {
                csvWriter.close();
            }
        }

        private void writeRecord(FileWriter csvWriter, String jarName, String licenseName, String licenseUrl) throws IOException {
            csvWriter.write(String.format("%s,%s,%s%n", jarName, safeString(licenseName), safeString(licenseUrl)));
        }

        public String safeString(final String s) {
            if(s == null) {
                return "";
            }
            return s.replaceAll("\\,", " ");
        }
    }

    private static interface LicenseOutput {

        abstract void output(final Map<String, List<License>> licensesPerFile) throws IOException;

    }

    static class MavenLogOutput implements LicenseOutput {
        private final Log log;

        public MavenLogOutput(final Log log) {
            super();
            this.log = log;
        }

        /**
         * @see net.kozelka.contentcheck.mojo.LicenseShowMojo.LicenseOutput#output(java.util.Map)
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
}
