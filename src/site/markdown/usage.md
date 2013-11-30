# Usage

Put the following XML fragment to a POM file producing an artifact (e.g. WAR or exploded directory) on which you would like to perform Maven Content Check Plugin's goals.

    <plugins>
        <plugin>
        <groupId>net.sf.buildbox.maven</groupId>
        <artifactId>contentcheck-maven-plugin</artifactId>
        <version>1.0.0</version>
            <configuration>
                <!-- optional configuration see below -->
            </configuration>
        </plugin>
    </plugins>


TODO: add sequence of recommended steps here

## contentcheck-maven-plugin:check

The goal checks a content of specified source file (typically project artifact) and reports unexpected or missing entries.

Create "content.txt" text file and put this file into "src/main" directory of the some module. This file contains allowed and also expected entries that the source file should contain. You can also let the goal 'generate' create initial version of this file.

Now you can run the goal by following command, but  ensure that the archive is present in module's target directory.

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:check``


## contentcheck-maven-plugin:generate

The goal generates a content definition from a given source.

You can run the goal by following command

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:generate``

## contentcheck-maven-plugin:show-licenses

The goal shows license information for a source's entries. License information is gathered from dependency's POM, but a project may define additional mapping between files in a project archive and licenses. Please see section Additional license information structure.

You can run the goal by following command

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:show-licenses``

