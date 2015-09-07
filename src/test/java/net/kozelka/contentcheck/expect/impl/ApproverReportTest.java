package net.kozelka.contentcheck.expect.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import net.kozelka.contentcheck.expect.TestUtils;
import net.kozelka.contentcheck.expect.api.ApproverReport;
import net.kozelka.contentcheck.expect.model.ActualEntry;
import net.kozelka.contentcheck.expect.model.ApprovedEntry;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApproverReportTest {

    private ApproverReport output;

    @Before
    public void setUp() {
        final Set<ApprovedEntry> approvedEntries = new LinkedHashSet<ApprovedEntry>() {
            {
                add(urientry("a.jar"));
                add(urientry("b.jar"));
                add(urientry("c.jar"));
                add(urientry("x-1.2.*.jar"));
            }
        };
        final Set<ActualEntry> sourceContent = new LinkedHashSet<ActualEntry>() {
            {
                add(TestUtils.newActualEntry("a.jar"));
                add(TestUtils.newActualEntry("d.jar"));
                add(TestUtils.newActualEntry("x-1.2.3.jar"));
            }
        };
        this.output = ContentChecker.compareEntries(approvedEntries, sourceContent);
    }

    private static ApprovedEntry urientry(String uri) {
        final ApprovedEntry result = new ApprovedEntry();
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
        final Set<ActualEntry> unexpectedEntries = output.getUnexpectedEntries();
        Assert.assertThat(unexpectedEntries, CoreMatchers.notNullValue());
        Assert.assertThat("Wrong count of unexpected entries.", unexpectedEntries.size(), CoreMatchers.is(1));
        Assert.assertThat("Concrete unexpected entry is missing.", TestUtils.contains(unexpectedEntries, "d.jar"), CoreMatchers.is(true));
    }

    @Test
    public void testDiffMissingEntries() {
        final Set<ApprovedEntry> missingEntries = output.getMissingEntries();
        Assert.assertThat(missingEntries, CoreMatchers.notNullValue());
        Assert.assertThat("Wrong count of missing entries.", missingEntries.size(), CoreMatchers.is(2));
        Assert.assertThat(ContentChecker.entrysetContainsUri(missingEntries, "b.jar"), CoreMatchers.is(true));
        Assert.assertThat(ContentChecker.entrysetContainsUri(missingEntries, "c.jar"), CoreMatchers.is(true));
    }

}
