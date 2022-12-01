package net.kozelka.contentcheck.mojo;

import java.io.File;
import java.util.List;
import java.util.Map;
import net.kozelka.contentcheck.SupportUtils;
import org.apache.maven.model.License;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class LicenseMappingParserTest {

    @Test
    public void testParseLicenseMappingFile()  throws Exception{
        final File licenseMappingFile = SupportUtils.getFile("license.mapping.json");
        final Map<String, List<License>> mapping = LicenseShow.parseLicenseMappingFile(licenseMappingFile);
        MatcherAssert.assertThat("Mapping parser didn't return any result object", mapping, notNullValue());
        MatcherAssert.assertThat("Mapping parser returned wrong number of entities", mapping.size(), is(2));
        final List<License> list = mapping.get("spring-beans-3.0.4.RELEASE.jar");
        MatcherAssert.assertThat("Mapping parser returned wrong number of parsed licenses for file spring-beans-3.0.4.RELEASE.jar", list.size(), is(1));
        final License license = list.get(0);
        MatcherAssert.assertThat("License's name doesn't match.", license.getName(), is("The Apache Software License  Version 2.0"));
        MatcherAssert.assertThat("License'URL doesn't match", license.getUrl(), is("http://www.apache.org/licenses/LICENSE-2.0.txt"));
    }

}
