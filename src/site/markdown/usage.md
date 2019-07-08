# Usage

Put the following XML fragment to a POM file producing an artifact (e.g. WAR or exploded directory) on which you would like to perform Maven Content Check Plugin's goals.

    <plugins>
        <plugin>
        <groupId>net.kozelka.maven</groupId>
        <artifactId>contentcheck-maven-plugin</artifactId>
        <version>@{project.version}</version>
            <configuration>
                <!-- optional configuration see below -->
            </configuration>
        </plugin>
    </plugins>


TODO: add sequence of recommended steps here

* add basic plugin definition
* generate initial list
* edit the list - add regexes, review libraries, ...
* commit first checking version

## contentcheck-maven-plugin:check

The goal checks a content of specified source file (archive or directory, typically project artifact) and reports unexpected or missing entries.

Create "content.txt" text file and put this file into "src/main" directory of the some module. This file contains allowed and also expected entries that the source file should contain. You can also let the goal 'generate' create initial version of this file.

Now you can run the goal by following command, but ensure that the archive is present in module's target directory.

``mvn net.kozelka.maven:contentcheck-maven-plugin:check``

(Or just `mvn contentcheck:check` if you have this plugin defined in pluginManagement or plugins.)


## contentcheck-maven-plugin:init

Convenient way to start using contentcheck plugin, with all its capabilities enabled. Use it to make all following at once:
* generate the <code>approved-content.txt</code> file to match current war content
* count war overlaps
* TODO: show resolution hints
* generate POM fragment with complete configuration (under target/contentcheck-maven-plugin/)
* operate on each module of multi-module project
* TODO: optionally, adjust POM configuration: `-DeditPom=true`

You can run the goal by following command

``mvn net.kozelka.maven:contentcheck-maven-plugin:init``

(Or just `mvn contentcheck:init` if you have this plugin defined in pluginManagement or plugins.)

## contentcheck-maven-plugin:generate - will be deprecated in next versions, use goal `init` instead

The goal generates a content definition from a given source.

You can run the goal by following command

``mvn net.kozelka.maven:contentcheck-maven-plugin:generate``

(Or just `mvn contentcheck:generate` if you have this plugin defined in pluginManagement or plugins.)

## contentcheck-maven-plugin:show-licenses

The goal shows license information for a source's entries. License information is gathered from dependency's POM, but a project may define additional mapping between files in a project archive and licenses. Please see section Additional license information structure.

You can run the goal by following command

``mvn net.kozelka.maven:contentcheck-maven-plugin:show-licenses``

(Or just `mvn contentcheck:init` if you have this plugin defined in pluginManagement or plugins.)
