package net.sf.buildbox.maven.contentcheck.dependencies;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class LicenseMappingParser {

    private final Log log;
    private final File licenseMappingFile;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LicenseMappingParser(final Log log, final File licenseMappingFile) {
        super();
        this.log = log;
        this.licenseMappingFile = licenseMappingFile;
    }

    public Map<String, List<License>> parseLicenseMappingFile() throws IOException, MojoFailureException{
        String licenseMappingFilePath = licenseMappingFile.getPath();
        Map<String, Object> json;
        try {
            log.info(String.format("Reading license mapping file %s", licenseMappingFilePath));
            json= objectMapper.readValue(licenseMappingFile, Map.class);
        } catch (JsonParseException e) {
            throw new MojoFailureException(String.format("Cannot parse JSON from file %s the content of the file is not well formed JSON.", licenseMappingFilePath),e);
        } catch (JsonMappingException e) {
            throw new MojoFailureException(String.format("Cannot deserialize JSON from file %s", licenseMappingFilePath),e);
        }
        Map <String, List<License>> fileToLicenseMapping = new HashMap<String, List<License>>();
        List<Map<String, Object>> jsonLicenses = (List<Map<String, Object>>) json.get("licenses");
        //construct model objects from JSON
        for (Map<String, Object> jsonLicense : jsonLicenses) {
            License license = new License();
            license.setName((String) jsonLicense.get("name"));
            license.setUrl((String) jsonLicense.get("url"));
            List<String> fileNames = (List<String>) jsonLicense.get("files");
            for (String fileName : fileNames) {
                fileToLicenseMapping.put(fileName, Arrays.asList(license));
            }
        }
        return fileToLicenseMapping;
    }

}
