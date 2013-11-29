## Configuration
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

The plugin might be configured by following properties. If a property is valid only for some goals then a goal name will be specified.

* *archive* The archive file to be checked. If *directory* is specified then *archive* is ignored.
 * Default ``${project.build.directory}/${project.build.finalName}.${project.packaging}``

* *contentListing* (valid for goals: check, generate) The content definition file (source of true)
 * Default ``src/main/content.txt``

* *checkFilesPattern* An Ant like file pattern determines which files will be checked int the archive
 * Default ``WEB-INF/lib/*.jar`` (All JARs in WAR)

* *directory* The directory file to be checked, takes a precedence before *archive*
 * No default

* *failOnUnexpected* (valid for goals: check) Break the build when the source contains files that are not declared in the content definition file.
 * Default ``true``

* *failOnMissing*  (valid for goals: check) Break the build when the source doesn't contain all files that are declared in the content definition file.
 * Default ``false``

* *ignoreVendorArchives* If it's true then doesn't check vendor JAR files. A vendor JAR file is determined by a value (*vendorId*) in its manifest key (*manifestVendorEntry*).
 * Default ``false``

* *vendorId*  The vendor id. This value is used for JAR's manifest checking when *noCheckVendorArchives* is turned on.

* *manifestVendorEntry* The name of manifest entry that holds vendor's identification. This value is used for JAR's manifest checking when *noCheckVendorArchives* is turned on.
 * Default ``Implementation-Vendor-Id``

* *msgMissing* (valid for goals: check) Message used to report missing entry - uses the ``java.util.Formatter`` syntax to embed entry name.
 * Default  ``File is expected but not found: %s``

* *msgUnexpected* (valid for goals: check) Message used to report unexpected entry - uses the ``java.util.Formatter`` syntax to embed entry name.
 * Default ``Found unexpected file: %s``

* *skipPOMPackaging* modules with POM packaging are skipped (excluded from the content check)
 * Default value is ``false``

* *overwriteExistingListing* (valid for goals: generate) If it's true the 'generate' goal overwrite content listing file.
 * Default ``false``

* *csvOutput* (valid for goals: show-licenses) If true print the result of check to a CSV file in project build directory.
  * Default ``true``

* *csvOutputFile* (valid for goals: show-licenses) The CSV output file that is used when (*csvOutput*) is turned on.
    * Default ``src/main/license.mapping``

Put the desired configuration properties into  ``configuration`` element

        <configuration>
            <${propertyName}>${value}</${propertyName}>
        </configuration>


