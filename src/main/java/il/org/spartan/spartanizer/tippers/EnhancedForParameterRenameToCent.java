package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.Utils.*;
import static il.org.spartan.lisp.*;
import static il.org.spartan.spartanizer.engine.JavaTypeNameParser.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;

/** Convert <code>for(int i:as)sum+=i;</code> to <code>f(int ¢:as)sum+=¢;</code>
 * @author Yossi Gil
 * @author Doron Meshulam
 * @since 2016-09 */
public final class EnhancedForParameterRenameToCent extends EagerTipper<EnhancedForStatement> implements TipperCategory.Centification {
  @Override public String description(final EnhancedForStatement ¢) {
    return "Rename '" + ¢.getParameter().getName() + "' to ¢ in enhanced for loop";
  }

  @Override public Tip tip(final EnhancedForStatement s, final ExclusionManager m) {
    final MethodDeclaration p = searchAncestors.forClass(MethodDeclaration.class).from(s);
    if (p == null)
      return null;
    final SimpleName sn = name(onlyOne(parameters(p)));
    if (sn == null || in(sn.getIdentifier(), "¢"))
      return null;
    final SingleVariableDeclaration d = s.getParameter();
    final SimpleName $ = d.getName();
    if (in(identifier($), "$", "¢", "__", "_") || !isJohnDoe(d))
      return null;
    final Statement body = s.getBody();
    if (haz.variableDefinition(body) || haz.cent(body) || Collect.usesOf($).in(body).isEmpty())
      return null;
    final SimpleName ¢ = s.getAST().newSimpleName("¢");
    if (m != null)
      m.exclude(s);
    return new Tip(description(s), s, this.getClass(), body) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        Tippers.rename($, ¢, s, r, g);
      }
    };
  }
}
