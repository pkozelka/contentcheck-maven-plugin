## Ignore your own artifacts

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

    <plugin>
        <groupId>net.kozelka.maven</groupId>
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
