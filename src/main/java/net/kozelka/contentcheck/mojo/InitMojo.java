package net.kozelka.contentcheck.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Convenient way to start using contentcheck plugin, with all its capabilities enabled.
 * Use it to make all following at once:
 * <ul>
 *     <li>TODO: generate the <code>approved-content.txt</code> file to match current war content</li>
 *     <li>TODO: count war conflicts, show resolution hints</li>
 *     <li>TODO: generate POM fragment with complete configuration</li>
 *     <li>TODO: operate on each module of multi-module project</li>
 *     <li>TODO: optionally, adjust POM configuration</li>
 * </ul>
 */
@Mojo(name = "init")
public class InitMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        throw new UnsupportedOperationException(); //TODO
    }
}
