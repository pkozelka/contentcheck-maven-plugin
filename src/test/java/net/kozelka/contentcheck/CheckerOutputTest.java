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
        Set<String> allowedEntries = new LinkedHashSet<String>() {
            {
                add("a.jar");
                add("b.jar");
                add("c.jar");
                add("x-1.2.*.jar");
            }
        };
        
        Set<String> sourceContent = new LinkedHashSet<String>() {
            {
                add("a.jar");
                add("d.jar");
                add("x-1.2.3.jar");
            }
        };
        
        this.output = new CheckerOutput(allowedEntries, sourceContent);
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
        Set<String> unexpectedEntries = output.diffUnexpectedEntries();
        assertThat(unexpectedEntries, notNullValue());
        assertThat("Wrong count of unexpected entries.", unexpectedEntries.size(), is(1));
        assertThat("Concrete unexpected entry is missing.", unexpectedEntries.contains("d.jar"), is(true));
    }

    @Test
    public void testDiffMissingEntries() {
        Set<String> missingEntries = output.diffMissingEntries();
        assertThat(missingEntries, notNullValue());
        assertThat("Wrong count of missing entries.", missingEntries.size(), is(2));
        assertThat(missingEntries.contains("b.jar"), is(true));
        assertThat(missingEntries.contains("c.jar"), is(true));
    }
}