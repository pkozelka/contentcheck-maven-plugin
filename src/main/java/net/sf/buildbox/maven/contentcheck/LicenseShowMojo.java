package net.sf.buildbox.maven.contentcheck;

import static net.sf.buildbox.maven.contentcheck.PathUtils.stripJARNameFromPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.buildbox.maven.contentcheck.dependencies.CsvOutput;
import net.sf.buildbox.maven.contentcheck.dependencies.LicenseMappingParser;
import net.sf.buildbox.maven.contentcheck.dependencies.LicenseOutput;
import net.sf.buildbox.maven.contentcheck.dependencies.LogOutput;
import net.sf.buildbox.maven.contentcheck.introspection.DefaultIntrospector;

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

/**
 * This MOJO shows license information for an archive entries. Artifact resolving
 * and the rest of Maven repo magic taken from <a href="http://maven.apache.org/plugins/maven-project-info-reports-plugin/index.html">Maven Project Info Reports Plugin</a>.
 * The MOJO shows license information only for entities that matches criteria
 * defined by {@link #getCheckFilesPattern()} and {@link #isIgnoreVendorArchives()}.
 * <p>
 * The MOJO's output is printed by default to console log and also to CSV file
 * <cite>${project.build.directory}/licenses.csv</cite>.
 * <p>
 * License information is gathered from dependency's POM, but a project may define
 * additional mapping between files in a project archive and licenses. This mapping
 * file is <cite>src/main/license.mapping</cite> and its structure is JSON.
 * <h4>Additional license information</h4>
 * <pre><code>
 * {
 * "licenses": [
 *       {
 *          "name" : "License name",
 *           "url"  : "License text URL",
 *           "files": [
 *               "file name"
 *            ]
 *       }
 *  ]
 * }
 * </code></pre>
 * <h4>Example</h4>
 * <pre><code>
 * {
 * "licenses": [
 *
 *       {
 *           "name"  : "The LGPL license 2.1",
 *           "url"   : "http://www.gnu.org/licenses/lgpl-2.1.html",
 *           "files" : [
 *                         "aspectwerkz-nodeps-jdk5-2.2.1.jar"
 *           ]
 *       },
 *
 *       {
 *           "name"  : "The public domain",
 *           "url"   : "http://creativecommons.org/licenses/publicdomain/",
 *           "files" : [
 *               "jsr166x-1.0.jar",
 *               "xyz.jar"
 *           ]
 *       }
 *   ]
 * }
 * </code></pre>
 *
 * @goal show-licenses
 */
public class LicenseShowMojo extends AbstractArchiveContentMojo{


    /**
     * The license mapping file. This file may define additional license information
     * for JARs that are not recognized.
     *
     * @parameter default-value="src/main/license.mapping"
     */
    private File licenseMappingFile;


    /**
     * If true print the result of check to a CSV file in project build directory.
     * See {@link #csvOutputFile}
     *
     * @parameter default-value="true"
     */
    private boolean csvOutput;

    /**
     * The CSV output file that is used when {@link #csvOutput} is turned on.
     *
     * @parameter default-value="${project.build.directory}/licenses.csv"
     */
    private File csvOutputFile;

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Artifact collector component.
     *
     * @component
     */
    private ArtifactCollector collector;

    /**
     * Artifact Factory component.
     *
     * @component
     */
    private ArtifactFactory factory;

    /**
     * Dependency tree builder component.
     *
     * @since 2.1
     * @component
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * Artifact metadata source component.
     *
     * @component
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * Jar classes analyzer component.
     *
     * @since 2.1
     * @component
     */
    private JarClassesAnalysis classesAnalyzer;

    /**
     * Local Repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Maven Project Builder component.
     *
     * @component
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * The current user system settings for use in Maven.
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     * @since 2.3
     */
    protected Settings settings;

    /**
     * Wagon manager component.
     *
     * @since 2.1
     * @component
     */
    private WagonManager wagonManager;

    /**
     * Artifact Resolver component.
     *
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * Repository metadata component.
     *
     * @since 2.1
     * @component
     */
    private RepositoryMetadataManager repositoryMetadataManager;

    /**
     * @see net.sf.buildbox.maven.contentcheck.AbstractArchiveContentMojo#doExecute()
     */
    @Override
    protected void doExecute() throws IOException, MojoExecutionException, MojoFailureException {
        List<MavenProject> mavenProjectForDependencies = getMavenProjectForDependencies();

        DefaultIntrospector introspector = new DefaultIntrospector(getLog(), isIgnoreVendorArchives(), getVendorId(), getManifestVendorEntry(), getCheckFilesPattern());
        introspector.readArchive(getArchive());

        Set<String> archiveEntries = new LinkedHashSet<String>(introspector.getArchiveEntries());
        Map<String, List<License>> entries = new LinkedHashMap<String, List<License>>();

        Map<String, List<License>> additionalLicenseInformation = new LinkedHashMap<String, List<License>>();
        if(licenseMappingFile != null && licenseMappingFile.exists()) {
            //read additional license information
            LicenseMappingParser licenseMappingParser = new LicenseMappingParser(getLog(), licenseMappingFile);
            additionalLicenseInformation.putAll(licenseMappingParser.parseLicenseMappingFile());
        }

        getLog().info("Comparing the archive content with Maven project artifacts");
        for(String archiveEntry : archiveEntries) {
            List<License> licenses = null; //these licenses will be associated with the given archive entry

            for (MavenProject mavenProject : mavenProjectForDependencies) {
                mavenProject.getGroupId();
                String artifactId = mavenProject.getArtifactId();
                String version = mavenProject.getVersion();
                String jarName = artifactId + "-" + version + ".jar"; //guess jar name
                if(archiveEntry.endsWith(jarName)) {
                    List _licenses = mavenProject.getLicenses();
                    licenses = _licenses == null || _licenses.size() == 0 ? null : _licenses  ;
                    break;
                }
            }

            List<License> licensesMappingFile = additionalLicenseInformation.get(stripJARNameFromPath(archiveEntry));

            if(licenses == null && licensesMappingFile == null) {//misising license information
                getLog().debug(String.format("Cannot resolve license information for archive entry %s neither from the POM file nor the file for license mapping", archiveEntry));
                //archive entry must be present even if there is no a matching Maven Project
                entries.put(archiveEntry, Collections.EMPTY_LIST);
            } else if(licenses != null && licensesMappingFile != null) {//licenses specified in both - POM and license mapping file
                getLog().warn(String.format("The license information for file %s are defined in the POM file and also in the file for license mapping. Using license information from the the file for license mapping.", archiveEntry));
                entries.put(archiveEntry, licensesMappingFile); //mapping manually specified licenses precedes licenses from POM
            } else if (licenses != null) {//license information in POM
                entries.put(archiveEntry, licenses);//license
            } else if (licensesMappingFile != null) {//license information in mapping file
                //put additional license information to the object that holds this information
                entries.put(archiveEntry, licensesMappingFile);
            }
        }

        LicenseOutput logOutput = new LogOutput(getLog());
        logOutput.output(entries);

        if(csvOutput) {
            CsvOutput csvOutput = new CsvOutput(getLog(), csvOutputFile);
            csvOutput.output(entries);
        }
    }

    private List<MavenProject> getMavenProjectForDependencies() throws MojoExecutionException, MojoFailureException {
        DependencyNode dependencyTreeNode = resolveProject();
        Dependencies dependencies = new Dependencies( project, dependencyTreeNode, classesAnalyzer );
        Log log = getLog();
        RepositoryUtils repoUtils = new RepositoryUtils( log, wagonManager, settings, mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(), project.getPluginArtifactRepositories(), localRepository,repositoryMetadataManager );
        Artifact projectArtifact = project.getArtifact();
        log.info(String.format("Resolving project %s:%s:%s dependencies", projectArtifact.getGroupId(), projectArtifact.getArtifactId(), projectArtifact.getVersion()));
        List<Artifact> allDependencies = dependencies.getAllDependencies();
        List<MavenProject> mavenProjects = new ArrayList<MavenProject>();
        for (Artifact artifact : allDependencies) {
            log.debug(String.format("Resolving project information for %s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
            try {
                MavenProject mavenProject = repoUtils.getMavenProjectFromRepository(artifact);
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
            ArtifactFilter artifactFilter = new ScopeArtifactFilter( Artifact.SCOPE_TEST );
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
}
