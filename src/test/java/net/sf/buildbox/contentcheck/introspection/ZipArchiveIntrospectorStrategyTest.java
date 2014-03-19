package net.sf.buildbox.contentcheck.introspection;

import net.sf.buildbox.contentcheck.SupportUtils;

public class ZipArchiveIntrospectorStrategyTest extends AbstractIntrospectorStrategyTest {
    public ZipArchiveIntrospectorStrategyTest() {
        super(new ZipArchiveIntrospectorStrategy(), SupportUtils.getFile("test.war"));
    }
}
