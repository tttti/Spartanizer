package il.org.spartan.spartanizer.tippers;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;

/** like (@link ForRedundantContinue) but for enhanced for. 
 * 
 * @author Kfir Marx
 * @since 2016-11-26 */
public class EnhancedForRedundantConinue extends CarefulTipper<EnhancedForStatement> implements TipperCategory.SyntacticBaggage {
  @Override public String description(EnhancedForStatement ¢) {
    return "Eliminate redundant " + lastStatement(¢);
  }
  
  @Override public String description() {
    return "Eliminate redundant continue";
  }
  
  static Statement lastStatement(final EnhancedForStatement ¢) {
    return !iz.block(¢.getBody()) ? ¢.getBody()
        : (Statement) (az.block(¢.getBody()).statements()).get((az.block(¢.getBody()).statements().size() - 1));
  }
  

  @Override public Tip tip(final EnhancedForStatement ¢) {
    return new Tip(description(¢), ¢, this.getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) { 
       remove(r,lastStatement(¢), g);
      }
    };
  }

  
  @Override public boolean prerequisite(final EnhancedForStatement ¢) {
   return ((lastStatement(¢)).getNodeType() == ASTNode.CONTINUE_STATEMENT);
  }
  
  public static void remove(final ASTRewrite r, final Statement s, final TextEditGroup g) {
    r.getListRewrite(parent(s), Block.STATEMENTS_PROPERTY).remove(s, g); 
  }

}