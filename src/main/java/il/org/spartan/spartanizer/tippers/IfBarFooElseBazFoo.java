package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.spartanizer.dispatch.Tippers.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;
import static il.org.spartan.lisp.*;
import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;

/** convert
 *
 * <pre>
 * if (X) {
 *   bar();
 *   foo();
 * } else {
 *   baz();
 *   foo();
 * }
 * </pre>
 *
 * into
 *
 * <pre>
 * if (X)
 *   bar();
 * else
 *   baz();
 * foo();
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-09-05 */
public final class IfBarFooElseBazFoo extends EagerTipper<IfStatement> implements TipperCategory.Ternarization {
  private static List<Statement> commmonSuffix(final List<Statement> ss1, final List<Statement> ss2) {
    final List<Statement> $ = new ArrayList<>();
    for (; !ss1.isEmpty() && !ss2.isEmpty(); ss2.remove(ss2.size() - 1)) {
      final Statement s1 = last(ss1);
      if (!wizard.same(s1, last(ss2)))
        break;
      $.add(s1);
      ss1.remove(ss1.size() - 1);
    }
    return $;
  }

  @Override public String description(@SuppressWarnings("unused") final IfStatement __) {
    return "Consolidate commmon suffix of then and else branches to just after if statement";
  }

  @Override public Tip tip(final IfStatement s) {
    final List<Statement> $ = extract.statements(then(s));
    if ($.isEmpty())
      return null;
    final List<Statement> elze = extract.statements(elze(s));
    if (elze.isEmpty())
      return null;
    final List<Statement> commmonSuffix = commmonSuffix($, elze);
    for (final Statement st : commmonSuffix) {
      final DefinitionsCollector c = new DefinitionsCollector($);
      st.accept(c);
      if (c.notAllDefined())
        return null;
    }
    return $.isEmpty() && elze.isEmpty() || commmonSuffix.isEmpty() ? null : new Tip(description(s), s, this.getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final IfStatement newIf = replacement();
        if (iz.block(s.getParent())) {
          final ListRewrite lr = insertAfter(s, commmonSuffix, r, g);
          lr.insertAfter(newIf, s, g);
          lr.remove(s, g);
        } else {
          if (newIf != null)
            commmonSuffix.add(0, newIf);
          r.replace(s, subject.ss(commmonSuffix).toBlock(), g);
        }
      }

      IfStatement replacement() {
        return replacement(s.getExpression(), subject.ss($).toOneStatementOrNull(), subject.ss(elze).toOneStatementOrNull());
      }

      IfStatement replacement(final Expression condition, final Statement trimmedThen, final Statement trimmedElse) {
        return trimmedThen == null && trimmedElse == null ? null
            : trimmedThen == null ? subject.pair(trimmedElse, null).toNot(condition) : subject.pair(trimmedThen, trimmedElse).toIf(condition);
      }
    };
  }

  @Override public Tip tip(final IfStatement s, final ExclusionManager exclude) {
    return super.tip(s, exclude);
  }

  private class DefinitionsCollector extends ASTVisitor {
    private boolean notAllDefined;
    private final Statement[] l;

    public DefinitionsCollector(final List<Statement> l) {
      notAllDefined = false;
      this.l = l.toArray(new Statement[l.size()]);
    }

    public boolean notAllDefined() {
      return notAllDefined;
    }

    @Override public boolean visit(final SimpleName ¢) {
      if (!Collect.declarationsOf(¢).in(l).isEmpty())
        notAllDefined = true;
      return false;
    }
  }
}
