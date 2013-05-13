package net.sf.buildbox.maven.contentcheck.introspection;

import net.sf.buildbox.maven.contentcheck.SupportUtils;

public class DirectoryIntrospectorStrategyTest extends AbstractIntrospectorStrategyTest {
    public DirectoryIntrospectorStrategyTest() {
        super(new DirectoryIntrospectorStrategy(), SupportUtils.getFile("test"));
    }
}
