package net.kozelka.contentcheck.expect.model;

/**
 * Represents the entry actually found in its container (like WAR file).
 * It is supposed to come with as much information as it is possible to obtain 'cheaply'.
 * This information will be used for matching and reporting.
 */
public class ActualEntry {

    private String uri;

    //TODO add other fields - maven coordinates, license, md5, sha1, ...

    /**
     * @return relative path within the container
     */
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "ActualEntry{" +
            "uri='" + uri + '\'' +
            '}';
    }
}
