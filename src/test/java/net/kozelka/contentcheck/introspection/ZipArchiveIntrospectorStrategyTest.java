package net.kozelka.contentcheck.introspection;

import net.kozelka.contentcheck.SupportUtils;

public class ZipArchiveIntrospectorStrategyTest extends AbstractIntrospectorStrategyTest {
    public ZipArchiveIntrospectorStrategyTest() {
        super(new ZipArchiveIntrospectorStrategy(), SupportUtils.getFile("test.war"));
    }
}
