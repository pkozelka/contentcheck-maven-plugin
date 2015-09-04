package net.kozelka.contentcheck.conflict.cli;

import java.io.File;
import net.kozelka.contentcheck.SupportUtils;
import org.junit.Test;

/**
 * @author Petr Kozelka
 */
public class ClassConflictMainTest {

    @Test
    public void testMain() throws Exception {
        final File archivaWar = SupportUtils.getFile("/archiva-webapp.war");
        ClassConflictMain.main(archivaWar.getAbsolutePath());
    }
}
