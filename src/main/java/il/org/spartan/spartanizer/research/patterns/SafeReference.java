package il.org.spartan.spartanizer.research.patterns;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.research.*;
import il.org.spartan.spartanizer.research.patterns.common.*;

/** Replace X == null ? null : X.Y with X?.Y <br>
 * replace X != null ? X.Y : null with X?.Y <br>
 * replace null == X ? null : X.Y with X?.Y <br>
 * replace null != X ? X.Y : null with X?.Y <br>
 * @author Ori Marcovitch
 * @year 2016 */
public final class SafeReference extends NanoPatternTipper<ConditionalExpression> {
  static Set<UserDefinedTipper<ConditionalExpression>> tippers = new HashSet<UserDefinedTipper<ConditionalExpression>>() {
    static final long serialVersionUID = 1L;
    {
      add(TipperFactory.patternTipper("$X1 == null ? null : $X1.$X2", "NullConditional($X1,$X2)", "null Conditional"));
      add(TipperFactory.patternTipper("$X1 != null ? $X1.$X2 : null", "NullConditional($X1,$X2)", "null Conditional"));
      add(TipperFactory.patternTipper("null == $X1 ? null : $X1.$X2", "NullConditional($X1,$X2)", "null Conditional"));
      add(TipperFactory.patternTipper("null != $X1 ? $X1.$X2 : null", "NullConditional($X1,$X2)", "null Conditional"));
    }
  };

  @Override public String description(@SuppressWarnings("unused") final ConditionalExpression __) {
    return "replace null conditionl ternary with ?.";
  }

  @Override public boolean canTip(final ConditionalExpression ¢) {
    return anyTips(tippers, ¢);
  }

  @Override public Tip pattern(final ConditionalExpression ¢) {
    return firstTip(tippers, ¢);
  }
}
