# Maven Content Check Plugin

Maven Content Check plugin is able to perform various kind of content checks on project's output artifact. There is a couple of use cases for such kind of check.

* legal issues 
 * an archive must contain only approved 3rd party libraries 
 * an archive must contain a license file
* content completeness
 * an archive must/must not contain some files
 
# Goals

## contentcheck-maven-plugin:check

The goal checks an archive content and reports unexpected or missing entries.

### Usage

Create "content.txt" text file and put this file into "src/main" directory of the some module. This file contains allowed and also expected entries that the archive should contain. You can also let the goal 'generate' create initial version of this file. 

Now you can run the goal by following command, but  ensure that the archive is present in module's target directory.

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:check``


## contentcheck-maven-plugin:generate

The goal generates a content definition from a given archive.

You can run the goal by following command

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:generate``

## contentcheck-maven-plugin:show-licenses   

The goal shows license information for an archive entries.

You can run the goal by following command

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:show-licenses``

## Configuration
Put the following XML fragment to a POM file producing an archive (e.g. WAR) on which you would like to perform Maven Content Check Plugin's goals.

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

* *archive* The archive file to be checked
 * Default ``${project.build.directory}/${project.build.finalName}.${project.packaging}``

* *contentListing* (valid for goals: check, generate) The content definition file (source of true)
 * Default ``src/main/content.txt``

* *checkFilesPattern* An Ant like file pattern determines which files will be checked int the archive
 * Default ``WEB-INF/lib/*.jar`` (All JARs in WAR) 

* *failOnUnexpected* (valid for goals: check) Break the build when the archive contains files that are not declared in the content definition file.
 * Default ``true`` 
  
* *failOnMissing*  (valid for goals: check) Break the build when the archive doesn't contain all files that are declared in the content definition file.
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
 
* *overwriteExistingListing* (valid for goals: generate) If it's true the 'generate' goal overwrite content listing file.
 * Default false  

Put the desired configuration properties into  ``configuration`` element

        <configuration>
            <${propertyName}>${value}</${propertyName}>       
        </configuration>


### Ignore your own artifacts

It's quite common to ignore own  artifacts during checking. The plugin supports such case and is driven by JAR's manifest.  You have to ensure that all internal artifacts or at least artifacts that you would like to ignore have a specific key/value pair in manifest files. You can do that by JAR plugin.

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
           <archive>
               <manifestEntries>
                   <Implementation-Vendor-Id>com.example</Implementation-Vendor-Id>                   
               </manifestEntries>
          </archive>
       </configuration>
    </plugin>

JAR plugin will produce ``MANIFEST.MF`` with key/value pair ``Implementation-Vendor-Id: com.example``.
 
Content check plugin has to be configured to *ignoreVendorArchives* for a given *vendorId*.

    <plugins>
        <plugin>
        <groupId>net.sf.buildbox.maven</groupId>
        <artifactId>contentcheck-maven-plugin</artifactId>
        <version>1.0.0</version>
        <configuration>
            <vendorId>com.example</vendorId>
            <ignoreVendorArchives>true</ignoreVendorArchives>               
        </configuration>
    </plugin>

You can put to *vendorId* whatever you want. You can also use any other key instead of ``Implementation-Vendor-Id`` for matching. A key name is specified by property *manifestVendorEntry*.

       ... 
       <configuration>
          <manifestVendorEntry>Producer</manifestVendorEntry>
          <vendorId>com.example</vendorId>
          <ignoreVendorArchives>true</ignoreVendorArchives>               
       </configuration>
       ...

Matches every MANIFEST.MF with key/value pair ``Producer: com.example``.

## Content definition file structure

* empty lines and lines starting to # are ignored
* one entry per line
* path is relation to archive root

WAR's content definition

    # This line is ignored
    WEB-INF/lib/akka-core_2.8.0-0.10.jar
    WEB-INF/lib/aopalliance-1.0.jar
    WEB-INF/lib/asm-3.2.jar