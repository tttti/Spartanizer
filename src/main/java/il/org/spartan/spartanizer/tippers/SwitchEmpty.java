package il.org.spartan.spartanizer.tippers;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;
import static il.org.spartan.lisp.*;
import static il.org.spartan.spartanizer.ast.navigate.step.*;

/** convert
 *
 * <pre>
 * switch (x) {
 * case a:
 * }
 *
 * switch(x) {
 * default: (some commands)
 * }
 * </pre>
 *
 * into
 *
 * <pre>
 * (some commands)
 * </pre>
 *
 * . Tested in {@link Issue233}
 * @author Yuval Simon
 * @since 2016-11-20 */
public final class SwitchEmpty extends CarefulTipper<SwitchStatement> implements TipperCategory.Collapse {
  @Override public Tip tip(final SwitchStatement s) {
    return new Tip(description(s), s, this.getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final List<Statement> ll = statements(s);
        final String ss = !haz.sideEffects(expression(s)) ? "" : expression(s) + ";";
        if (noSideEffectCommands(s)) {
          r.remove(s, g);
          if (haz.sideEffects(expression(s)))
            r.replace(s, wizard.ast(ss), g);
          return;
        }
        if (iz.breakStatement(last(ll)))
          ll.remove(ll.size() - 1);
        ll.remove(0);
        r.replace(s, wizard.ast(ss + statementsToString(ll)), g);
      }
    };
  }

  static String statementsToString(final List<Statement> ss) {
    final StringBuilder $ = new StringBuilder();
    for (final Statement k : ss)
      $.append(k);
    return $ + "";
  }

  @Override protected boolean prerequisite(final SwitchStatement ¢) {
    final List<SwitchCase> $ = extract.switchCases(¢);
    return noSideEffectCommands(¢) || $.isEmpty() || $.size() == 1 && $.get(0).isDefault();
  }

  static boolean noSideEffectCommands(final SwitchStatement s) {
    final List<Statement> ll = statements(s);
    for (final Statement ¢ : ll)
      if (!iz.switchCase(¢) && !iz.breakStatement(¢))
        return false;
    return true;
  }

  @Override public String description(@SuppressWarnings("unused") final SwitchStatement __) {
    return "Remove empty switch statement or switch statement with only default case";
  }
}
