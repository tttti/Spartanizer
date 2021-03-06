package il.org.spartan.spartanizer.tippers;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;

/** Convert Infinite loops with return sideEffects to shorter ones : </br>
 * Convert <br/>
 * <code>
 * while (true) { <br/>
 * doSomething(); <br/>
 * if(done()) <br/>
 * break; <br/>
 * } <br/>
 *return XX; <br/>
 * </code> to : <br/>
 * <code> while (true) { <br/>
 * doSomething(); <br/>
 * if(done()) <br/>
 * return XX; <br/>
 * } <br/>
 * @author Dor Ma'ayan
 * @since 2016-09-09 */
public final class BlockBreakToReturnInfiniteWhile extends CarefulTipper<WhileStatement> implements TipperCategory.Collapse {
  private static Statement handleBlock(final Block body, final ReturnStatement nextReturn) {
    Statement $ = null;
    for (final Statement ¢ : step.statements(body)) {
      if (az.ifStatement(¢) != null)
        $ = handleIf(¢, nextReturn);
      if (iz.breakStatement(¢)) {
        $ = ¢;
        break;
      }
    }
    return $;
  }

  private static Statement handleIf(final Statement s, final ReturnStatement nextReturn) {
    final IfStatement ifStatement = az.ifStatement(s);
    if (ifStatement == null)
      return null;
    final Statement then = ifStatement.getThenStatement();
    final Statement elze = ifStatement.getElseStatement();
    if (then != null) {
      if (iz.breakStatement(then))
        return then;
      if (iz.block(then)) {
        final Statement $ = handleBlock((Block) then, nextReturn);
        if ($ != null)
          return $;
      }
      if (az.ifStatement(then) != null)
        return handleIf(then, nextReturn);
      if (elze != null) {
        if (iz.breakStatement(elze))
          return elze;
        if (iz.block(elze)) {
          final Statement $ = handleBlock((Block) elze, nextReturn);
          if ($ != null)
            return $;
        }
        if (az.ifStatement(elze) != null)
          return handleIf(elze, nextReturn);
      }
    }
    return null;
  }

  private static boolean isInfiniteLoop(final WhileStatement ¢) {
    return az.booleanLiteral(¢.getExpression()) != null && az.booleanLiteral(¢.getExpression()).booleanValue();
  }

  @Override public String description() {
    return "Convert the break inside 'while()' loop to 'return'";
  }

  @Override public String description(final WhileStatement ¢) {
    return "Convert the break inside 'while(" + ¢.getExpression() + ")' to return";
  }

  @Override public boolean prerequisite(final WhileStatement ¢) {
    return ¢ != null && extract.nextReturn(¢) != null && isInfiniteLoop(¢);
  }

  @Override public Tip tip(final WhileStatement b, final ExclusionManager exclude) {
    final ReturnStatement nextReturn = extract.nextReturn(b);
    if (b == null || !isInfiniteLoop(b) || nextReturn == null)
      return null;
    final Statement body = b.getBody();
    final Statement $ = iz.ifStatement(body) ? handleIf(body, nextReturn)
        : iz.block(body) ? handleBlock((Block) body, nextReturn) : iz.breakStatement(body) ? body : null;
    if (exclude != null)
      exclude.exclude(b);
    return $ == null ? null : new Tip(description(b), b, this.getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        r.replace($, nextReturn, g);
        r.remove(nextReturn, g);
      }
    };
  }
}
