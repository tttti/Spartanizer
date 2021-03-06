package il.org.spartan.spartanizer.tippers;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.tipping.*;

/** Convert <code>if(a){f();return;}g();</code> into
 * <code>if(a){f();return;}g();</code> f(); } </code> provided that this
 * <code><b>if</b></code> statement is the last statement in a method.
 * @author Yossi Gil
 * @since 2016 */
public final class IfPenultimateInMethodFollowedBySingleStatement extends ReplaceToNextStatement<IfStatement> implements TipperCategory.EarlyReturn {
  public static void remove(final ASTRewrite r, final Statement s, final TextEditGroup g) {
    r.getListRewrite(parent(s), Block.STATEMENTS_PROPERTY).remove(s, g);
  }

  static <T> void removeLast(final List<T> ¢) {
    ¢.remove(¢.size() - 1);
  }

  @Override public String description(final IfStatement ¢) {
    return "Convert return into else in  if(" + ¢.getExpression() + ")";
  }

  @Override protected ASTRewrite go(final ASTRewrite $, final IfStatement s, final Statement nextStatement, final TextEditGroup g) {
    if (elze(s) != null || !iz.lastInMethod(nextStatement))
      return null;
    final Statement then = then(s);
    final ReturnStatement deleteMe = az.returnStatement(hop.lastStatement(then));
    if (deleteMe == null || deleteMe.getExpression() != null)
      return null;
    $.replace(deleteMe, make.emptyStatement(deleteMe), g);
    remove($, nextStatement, g);
    final IfStatement newIf = duplicate.of(s);
    final Block block = az.block(then(newIf));
    if (block != null)
      removeLast(step.statements(block));
    else
      newIf.setThenStatement(make.emptyStatement(newIf));
    newIf.setElseStatement(duplicate.of(nextStatement));
    $.replace(s, newIf, g);
    remove($, nextStatement, g);
    return $;
  }
}
