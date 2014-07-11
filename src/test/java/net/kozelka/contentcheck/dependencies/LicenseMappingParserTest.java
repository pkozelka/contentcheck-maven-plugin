package net.kozelka.contentcheck.dependencies;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.kozelka.contentcheck.SupportUtils;

import org.apache.maven.model.License;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

public class LicenseMappingParserTest {

    @Test
    public void testParseLicenseMappingFile()  throws Exception{
        File licenseMappingFile = SupportUtils.getFile("license.mapping.json");
        Log log = mock(Log.class);
        LicenseMappingParser parser = new LicenseMappingParser(log, licenseMappingFile);
        Map<String, List<License>> mapping = parser.parseLicenseMappingFile();
        assertThat("Mapping parser didn't return any result object", mapping, notNullValue());
        assertThat("Mapping parser returned wrong number of entities", mapping.size(), is(2));
        List<License> list = mapping.get("spring-beans-3.0.4.RELEASE.jar");
        assertThat("Mapping parser returned wrong number of parsed licenses for file spring-beans-3.0.4.RELEASE.jar", list.size(), is(1));
        License license = list.get(0);
        assertThat("License's name doesn't match.", license.getName(), is("The Apache Software License  Version 2.0"));
        assertThat("License'URL doesn't match", license.getUrl(), is("http://www.apache.org/licenses/LICENSE-2.0.txt"));
    }

}
