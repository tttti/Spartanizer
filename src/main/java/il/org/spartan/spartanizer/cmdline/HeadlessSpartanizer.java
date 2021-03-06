package il.org.spartan.spartanizer.cmdline;

import java.io.*;

import il.org.spartan.external.*;
import il.org.spartan.spartanizer.cmdline.report.*;

/** A configurable version of the HeadlessSpartanizer that relies on
 * {@link CommandLineApplicator} and {@link CommandLineSelection}
 * @author Matteo Orru'
 * @since 2016 */
public class HeadlessSpartanizer extends AbstractCommandLineProcessor {
  @External(alias = "np", value = "Nano Patterns") private static String[] nanoPatterns;
  @External final CommandLineApplicator commandLineApplicator = new CommandLineApplicator();
  @External final boolean CommandLine$Applicator = true;
  @External boolean DefaultApplicator;
  @External String name;
  @External boolean selection;
  @External boolean Spartanizer$Applicator;
  @External(alias = "cs", value = "class name on which apply the tippers") String[] classes;
  @External(alias = "allnp", value = "Exclude All Nano Patterns") boolean excludeAllNanoPatterns;
  @External(alias = "enp", value = "Exclude Selected Nano Patterns") String[] excludedNanoPatterns;
  @External(alias = "etg", value = "exclude one or more tipper groups") String[] excludedTipperGroups;
  @External(alias = "tg", value = "tipper group to be applied to the classes") String[] tipperGroups;

  public HeadlessSpartanizer() {
    this(".");
  }

  HeadlessSpartanizer(final String path) {
    this(path, system.folder2File(path));
  }

  HeadlessSpartanizer(final String presentSourcePath, final String name) {
    inputFolder = presentSourcePath;
    this.name = name;
  }

  @Override public void apply() {
    try {
      ReportGenerator.initializeFile(ReportGenerator.getOutputFolder() + "/" + name + ".before.java", "before");
      ReportGenerator.initializeFile(ReportGenerator.getOutputFolder() + "/" + name + ".after.java", "after");
      ReportGenerator.initializeReport(ReportGenerator.getOutputFolder() + "/" + name + ".CSV", "metrics");
      ReportGenerator.initializeReport(ReportGenerator.getOutputFolder() + "/" + name + ".spectrum.CSV", "spectrum");
      ReportGenerator.initializeReport(ReportGenerator.getOutputFolder() + "/" + name + ".tips.CSV", "tips");
      final CommandLineApplicator defaultApplicator2 = CommandLineApplicator.defaultApplicator();
      final CommandLineApplicator defaultSelection = defaultApplicator2
          .defaultSelection(CommandLineSelection.Util.get(ReportGenerator.getInputFolder()));
      if (DefaultApplicator) {
        commandLineApplicator.listener(¢ -> System.out.println("Running DefaultApplicator: " + ¢));
        defaultSelection.defaultListenerNoisy().go();
      }
      if (Spartanizer$Applicator)
        defaultSelection.defaultRunAction(new Spartanizer$Applicator()).defaultListenerNoisy().go();
      if (CommandLine$Applicator)
        defaultSelection.defaultRunAction(new CommandLine$Applicator(classes, tipperGroups, excludedTipperGroups, excludedNanoPatterns))
            .defaultListenerNoisy().go();
      ReportGenerator.close("metrics");
      ReportGenerator.close("spectrum");
      ReportGenerator.close("tips");
      ReportGenerator.closeFile("before");
      ReportGenerator.closeFile("after");
      System.err.println("commandLineApplicator: " + "Done!");
      if (selection)
        defaultApplicator2.defaultListenerNoisy()
            .defaultSelection(CommandLineSelection.of(CommandLineSelection.Util.getAllCompilationUnits(inputFolder)))
            .defaultRunAction(new CommandLine$Applicator()).go();
    } catch (final IOException ¢) {
      ¢.printStackTrace();
    }
  }

  public void name(final String ¢) {
    name = ¢;
  }
}
