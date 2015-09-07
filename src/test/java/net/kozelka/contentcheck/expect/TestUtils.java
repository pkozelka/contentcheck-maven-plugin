package net.kozelka.contentcheck.expect;

import net.kozelka.contentcheck.expect.model.ActualEntry;

/**
 * @author Petr Kozelka
 */
public final class TestUtils {
    public static boolean contains(Iterable<ActualEntry> actualEntries, String uri) {
        for (ActualEntry actualEntry : actualEntries) {
            if (uri.equals(actualEntry.getUri())) return true;
        }
        return false;
    }

    public static ActualEntry newActualEntry(String uri) {
        final ActualEntry actualEntry = new ActualEntry();
        actualEntry.setUri(uri);
        return actualEntry;
    }
}
