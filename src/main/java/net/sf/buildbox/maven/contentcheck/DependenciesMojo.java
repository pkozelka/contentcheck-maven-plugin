package net.sf.buildbox.maven.contentcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
 *
 * @goal show-licenses
 */
public class DependenciesMojo extends AbstractArchiveContentMojo{

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
        Set<String> unknownEntries = new LinkedHashSet<String>(archiveEntries);
        Set<String> knownEntries = new LinkedHashSet<String>();
        getLog().info("Comparing the archive content with Maven project artifacts");
        for (MavenProject mavenProject : mavenProjectForDependencies) {
            mavenProject.getGroupId();
            String artifactId = mavenProject.getArtifactId();
            String version = mavenProject.getVersion();
            String jarName = artifactId + "-" + version + ".jar";
            for(String archiveEntry : archiveEntries) {
                if(archiveEntry.endsWith(jarName)) {
                    List<License> licenses = mavenProject.getLicenses();
                    if(licenses.size() > 1) {
                        String l = "";
                        for(License licence : licenses) {
                            l+= licence.getName() + "(" +  licence.getUrl() + ") ";
                        }
                        knownEntries.add(String.format("%s has multiple licenses %s", archiveEntry, l));
                        unknownEntries.remove(archiveEntry);
                    } else if(licenses.size() == 1) {
                        License licence = licenses.get(0);
                        knownEntries.add(String.format("%s %s (%s)", archiveEntry, licence.getName(), licence.getUrl()));
                        unknownEntries.remove(archiveEntry);
                    }
                }
            }
        }

        if(unknownEntries.size() == 0) {
            getLog().info("All artifact entries have associated license information.");
        } else {
            getLog().warn("Some of the entries have no associated license information or the plugin wasn't able to determine them. Please check them manually.");
        }

        getLog().info("");
        getLog().info("The archive contains following entries with known license information:");
        for (String entryDesc : knownEntries) {
            getLog().info(entryDesc);
        }

        if(unknownEntries.size() > 0) {
            getLog().info("");
            getLog().warn("The archive contains following entries with uknown license inforamtion:");
            for (String archiveName : unknownEntries) {
                getLog().warn(archiveName);
            }
        }
    }

    private List<MavenProject> getMavenProjectForDependencies() throws MojoExecutionException, MojoFailureException {
        DependencyNode dependencyTreeNode = resolveProject();
        Dependencies dependencies = new Dependencies( project, dependencyTreeNode, classesAnalyzer );
        RepositoryUtils repoUtils = new RepositoryUtils( getLog(), wagonManager, settings, mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(), project.getPluginArtifactRepositories(), localRepository,repositoryMetadataManager );
        Artifact projectArtifact = project.getArtifact();
        getLog().info(String.format("Resolving project %s:%s:%s dependencies", projectArtifact.getGroupId(), projectArtifact.getArtifactId(), projectArtifact.getVersion()));
        List<Artifact> allDependencies = dependencies.getAllDependencies();
        List<MavenProject> mavenProjects = new ArrayList<MavenProject>();
        for (Artifact artifact : allDependencies) {
            getLog().debug(String.format("Resolving project information for %s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
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
}
