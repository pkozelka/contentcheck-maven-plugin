package net.kozelka.contentcheck.introspection;

import net.kozelka.contentcheck.SupportUtils;

public class DirectoryIntrospectorStrategyTest extends AbstractIntrospectorStrategyTest {
    public DirectoryIntrospectorStrategyTest() {
        super(new DirectoryIntrospectorStrategy(), SupportUtils.getFile("test"));
    }
}
