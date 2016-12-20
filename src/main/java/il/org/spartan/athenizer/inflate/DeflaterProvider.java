package il.org.spartan.athenizer.inflate;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.tipping.*;

/*
 * @author Raviv Rachmiel
 * @since 20-12-16
 * will hold an  toolbox for the expanders and return them
 */
public class DeflaterProvider extends OperationsProvider {
  Toolbox toolbox;
  
  public DeflaterProvider() {
    toolbox = Toolbox.defaultInstance();
    if(toolbox == null)
      toolbox = Toolbox.freshCopyOfAllTippers();
  }
  
  public DeflaterProvider(Toolbox tb) {
    this.toolbox = tb;
  }

  @Override public <N extends ASTNode> Tipper<N> getTipper(N n) {
    return toolbox.firstTipper(n);
  }
  
}
