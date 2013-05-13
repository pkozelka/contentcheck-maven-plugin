package net.sf.buildbox.maven.contentcheck.introspection;

import net.sf.buildbox.maven.contentcheck.SupportUtils;

public class ZipArchiveIntrospectorStrategyTest extends AbstractIntrospectorStrategyTest {
    public ZipArchiveIntrospectorStrategyTest() {
        super(new ZipArchiveIntrospectorStrategy(), SupportUtils.getFile("test.war"));
    }
}
