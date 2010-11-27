# Maven Content Check Plugin

The plugin checks an archive content and reports unexpected or missing entries. There is a couple of use cases for such kid of check.

* legal issues 
 * an archive must contain only approved 3rd party libraries 
 * an archive must contain a license file
* content completeness
 * an archive must/must not contain some files

## Usage

Note: the plugin is not yet available in any public Maven repository therefore you have to checkout source code and run `mvn install`in order to put the plugin in to your local repository or you can of course deploy the plugin to your corporate repository or whatever you have for hosting 3rd party artifacts. 

Put the following XML fragment to POM file producing the archive (e.g. WAR) you would like to check.

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

Create "content.txt" text file  and put this file into "src/main" directory of the some module. This file contains allowed and also expected entries that the archive should contain TODO link to file structure.

Now you can run the plugin (be sure that the archive is present in module's target directory)

``mvn net.sf.buildbox.maven:contentcheck-maven-plugin:1.0.0:check``

## Content definition file structure

* empty lines and lines starting to # are ignored
* one entry per line
* path is relation to archive root

WAR's content definition

    # This line is ignored
    WEB-INF/lib/akka-core_2.8.0-0.10.jar
    WEB-INF/lib/aopalliance-1.0.jar
    WEB-INF/lib/asm-3.2.jar


## Configuration


The plugin might be configured by following properties.

* *archive* The archive file to be checked
 * Default ``${project.build.directory}/${project.build.finalName}.${project.packaging}``

* *contentListing*  The content definition file (source of true)
 * Default ``src/main/content.txt``

* *checkFilesPattern* An Ant like file pattern determines which files will be checked int the archive
 * Default ``WEB-INF/lib/*.jar`` (All JARs in WAR) 

* *failOnUnexpected* Break the build when the archive contains files that are not declared in the content definition file.
 * Default ``true`` 
  
* *failOnMissing*  Break the build when the archive doesn't contain all files that are declared in the content definition file.
 * Default ``false``

* *ignoreVendorArchives* If it's true then doesn't check vendor JAR files. A vendor JAR file is determined by a value (*vendorId*) in its manifest key (*manifestVendorEntry*).
 * Default ``false``
   
* *vendorId*  The vendor id. This value is used for JAR's manifest checking when *noCheckVendorArchives* is turned on.
    
* *manifestVendorEntry* The name of manifest entry that holds vendor's identification. This value is used for JAR's manifest checking when *noCheckVendorArchives* is turned on.
 * Default ``Implementation-Vendor-Id``

* *msgMissing* Message used to report missing entry - uses the ``java.util.Formatter`` syntax to embed entry name.
 * Default  ``File is expected but not found: %s``

* *msgUnexpected* Message used to report unexpected entry - uses the ``java.util.Formatter`` syntax to embed entry name.
 * Default ``Found unexpected file: %s``

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