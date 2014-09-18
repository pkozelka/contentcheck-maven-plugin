package net.kozelka.contentcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class CheckerOutputTest {
    
    private CheckerOutput output;
    
    @Before
    public void setUp() {
        final Set<CheckerEntry> approvedEntries = new LinkedHashSet<CheckerEntry>() {
            {
                add(urientry("a.jar"));
                add(urientry("b.jar"));
                add(urientry("c.jar"));
                add(urientry("x-1.2.*.jar"));
            }
        };

        final Set<String> sourceContent = new LinkedHashSet<String>() {
            {
                add("a.jar");
                add("d.jar");
                add("x-1.2.3.jar");
            }
        };
        
        this.output = new CheckerOutput(approvedEntries, sourceContent);
    }

    private static CheckerEntry urientry(String uri) {
        final CheckerEntry result = new CheckerEntry();
        result.setUri(uri);
        return result;
    }

    @Test
    public void testGetAllowedEntries() {
        assertThat(output.getApprovedEntries(), notNullValue());
        assertThat(output.getApprovedEntries().size(), is(4));
    }

    @Test
    public void testGetSourceEntries() {
        assertThat(output.getActualEntries(), notNullValue());
        assertThat(output.getActualEntries().size(), is(3));
    }

    @Test
    public void testDiffUnexpectedEntries() {
        final Set<String> unexpectedEntries = output.getUnexpectedEntries();
        assertThat(unexpectedEntries, notNullValue());
        assertThat("Wrong count of unexpected entries.", unexpectedEntries.size(), is(1));
        assertThat("Concrete unexpected entry is missing.", unexpectedEntries.contains("d.jar"), is(true));
    }

    @Test
    public void testDiffMissingEntries() {
        final Set<CheckerEntry> missingEntries = output.getMissingEntries();
        assertThat(missingEntries, notNullValue());
        assertThat("Wrong count of missing entries.", missingEntries.size(), is(2));
        assertThat(ContentChecker.entrysetContainsUri(missingEntries, "b.jar"), is(true));
        assertThat(ContentChecker.entrysetContainsUri(missingEntries, "c.jar"), is(true));
    }

}