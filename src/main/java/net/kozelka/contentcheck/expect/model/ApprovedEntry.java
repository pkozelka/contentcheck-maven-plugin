package net.kozelka.contentcheck.expect.model;

/**
 * Abstraction that allows to specify various entry styles in the `approved-content.txt`.
 * The idea is to have support for uri, uriPattern, Maven GAV, GA(v) etc.
 * @author Petr Kozelka
 */
public class ApprovedEntry {
    //TODO: split to uri and uriPattern
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return uri;
    }
}
