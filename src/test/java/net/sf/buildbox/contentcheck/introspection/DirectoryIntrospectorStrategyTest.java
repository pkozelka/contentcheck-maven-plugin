package net.sf.buildbox.contentcheck.introspection;

import net.sf.buildbox.contentcheck.SupportUtils;

public class DirectoryIntrospectorStrategyTest extends AbstractIntrospectorStrategyTest {
    public DirectoryIntrospectorStrategyTest() {
        super(new DirectoryIntrospectorStrategy(), SupportUtils.getFile("test"));
    }
}
