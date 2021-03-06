package il.org.spartan.spartanizer.research.analyses;

import static il.org.spartan.lisp.*;
import static il.org.spartan.spartanizer.research.analyses.util.Files.*;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.core.dom.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.utils.*;
import il.org.spartan.utils.*;

/** @author Ori Marcovitch
 * @since Dec 14, 2016 */
public interface hIndex {
  static int hindex(final List<Pair<String, Int>> ¢) {
    for (int $ = 0; $ < ¢.size(); ++$) {
      if ($ > ¢.get($).second.inner)
        return $;
      System.out.println(¢.get($).first + " : " + ¢.get($).second.inner);
    }
    return ¢.size();
  }

  static void analyze() {
    final Map<String, Pair<String, Int>> ranking = new HashMap<>();
    for (final File f : inputFiles()) {
      final CompilationUnit cu = az.compilationUnit(compilationUnit(f));
      searchDescendants.forClass(MethodInvocation.class).from(cu).stream().forEach(m -> {
        final String key = declarationFile(cu, identifier(name(m)), f.getName()) + name(m) + "(" + arguments(m).size() + " params)";
        ranking.putIfAbsent(key, new Pair<>(key, new Int()));
        ++ranking.get(key).second.inner;
      });
    }
    final List<Pair<String, Int>> rs = new ArrayList<>();
    rs.addAll(ranking.values());
    rs.sort((x, y) -> x.second.inner > y.second.inner ? -1 : x.second.inner < y.second.inner ? 1 : 0);
    System.out.println("Max: " + first(rs).first + " [" + first(rs).second.inner + "]");
    System.out.println("min: " + last(rs).first + " [" + last(rs).second.inner + "]");
    System.out.println("h-index: " + hindex(rs));
  }

  static String declarationFile(final CompilationUnit u, final String methodName, final String fileName) {
    return !methodNames(u).contains(methodName) ? "" : fileName.replaceAll("\\.java", "") + ".";
  }
}
