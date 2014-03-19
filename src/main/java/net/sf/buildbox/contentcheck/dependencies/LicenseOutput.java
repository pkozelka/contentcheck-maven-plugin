package net.sf.buildbox.contentcheck.dependencies;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.License;

public interface LicenseOutput {

    public abstract void output(final Map<String, List<License>> entries) throws IOException;

}