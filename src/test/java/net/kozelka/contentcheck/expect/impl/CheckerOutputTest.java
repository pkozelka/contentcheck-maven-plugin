package net.kozelka.contentcheck.expect.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import net.kozelka.contentcheck.expect.api.CheckerOutput;
import net.kozelka.contentcheck.expect.model.CheckerEntry;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
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
        this.output = ContentChecker.compareEntries(approvedEntries, sourceContent);
    }

    private static CheckerEntry urientry(String uri) {
        final CheckerEntry result = new CheckerEntry();
        result.setUri(uri);
        return result;
    }

    @Test
    public void testGetAllowedEntries() {
        Assert.assertThat(output.getApprovedEntries(), CoreMatchers.notNullValue());
        Assert.assertThat(output.getApprovedEntries().size(), CoreMatchers.is(4));
    }

    @Test
    public void testGetSourceEntries() {
        Assert.assertThat(output.getActualEntries(), CoreMatchers.notNullValue());
        Assert.assertThat(output.getActualEntries().size(), CoreMatchers.is(3));
    }

    @Test
    public void testDiffUnexpectedEntries() {
        final Set<String> unexpectedEntries = output.getUnexpectedEntries();
        Assert.assertThat(unexpectedEntries, CoreMatchers.notNullValue());
        Assert.assertThat("Wrong count of unexpected entries.", unexpectedEntries.size(), CoreMatchers.is(1));
        Assert.assertThat("Concrete unexpected entry is missing.", unexpectedEntries.contains("d.jar"), CoreMatchers.is(true));
    }

    @Test
    public void testDiffMissingEntries() {
        final Set<CheckerEntry> missingEntries = output.getMissingEntries();
        Assert.assertThat(missingEntries, CoreMatchers.notNullValue());
        Assert.assertThat("Wrong count of missing entries.", missingEntries.size(), CoreMatchers.is(2));
        Assert.assertThat(ContentChecker.entrysetContainsUri(missingEntries, "b.jar"), CoreMatchers.is(true));
        Assert.assertThat(ContentChecker.entrysetContainsUri(missingEntries, "c.jar"), CoreMatchers.is(true));
    }

}
