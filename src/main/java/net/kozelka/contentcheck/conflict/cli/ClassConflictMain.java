package net.kozelka.contentcheck.conflict.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.kozelka.contentcheck.conflict.api.ClassConflictReport;
import net.kozelka.contentcheck.conflict.impl.ClassConflictAnalyzer;
import net.kozelka.contentcheck.conflict.impl.ClassConflictPrinter;
import net.kozelka.contentcheck.conflict.model.ArchiveInfo;
import net.kozelka.contentcheck.conflict.util.ArchiveLoader;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * <h1>CLI for class conflicts.</h1>
 * @todo Synopsis: cc [--jar-report] [--class-report] [jar|war|ear]
 * @author Petr Kozelka
 */
public class ClassConflictMain {
    public static void main(String[] args) throws IOException {
        final int previewThreshold = 5;
        final File war = new File(args[0]);

        System.out.println("Detecting conflict in " + war);
        System.out.println("Class preview threshold: " + previewThreshold);
        final ClassConflictAnalyzer analyzer = new ClassConflictAnalyzer();
        final List<ArchiveInfo> archives = ArchiveLoader.loadWar(war);
        final ClassConflictReport report = analyzer.analyze(archives);
        final ClassConflictPrinter printer = new ClassConflictPrinter();
        printer.setPreviewThreshold(previewThreshold);
        printer.setOutput(new StreamConsumer() {
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
        printer.print(report);
    }
}
