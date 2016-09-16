package il.org.spartan.spartanizer.wring.strategies;

import static java.lang.reflect.Modifier.*;

import java.lang.reflect.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.wring.dispatch.*;

/** A wring is a transformation that works on an AstNode. Such a transformation
 * make a single simplification of the tree. A wring is so small that it is
 * idempotent: Applying a wring to the output of itself is the empty operation.
 * @param <N> type of node which triggers the transformation.
 * @author Yossi Gil
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015-07-09 */
public abstract class Wring<N extends ASTNode> implements Kind {
  private static boolean isInstance(final Method ¢) {
    return !isStatic(¢.getModifiers());
  }

  private Class<N> myOperandsClass = null;

  /** Determine whether the parameter is "eligible" for application of this
   * instance. The parameter must be within the scope of the current instance.
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object. */
  public boolean canSuggest(@SuppressWarnings("unused") final N __) {
    return true;
  }

  /** Determines whether this {@link Wring} object is not applicable for a given
   * {@link PrefixExpression} is within the "scope" of this . Note that a
   * {@link Wring} is applicable in principle to an object, but that actual
   * application will be vacuous.
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is noneligible for
   *         the simplification offered by this object.
   * @see #canSuggest(InfixExpression) */
  public final boolean cantSuggest(final N ¢) {
    return !canSuggest(¢);
  }

  @SuppressWarnings("unchecked") private Class<N> castClass(final Class<?> c2) {
    return (Class<N>) c2;
  }

  /** Determines whether this {@link Wring} object is applicable for a given
   * {@link InfixExpression} is within the "scope" of this . Note that it could
   * be the case that a {@link Wring} is applicable in principle to an object,
   * but that actual application will be vacuous. If a wring claims a node, it
   * may be the case that the node would not be seen at all by other wrings
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object @ */
  @Deprecated public boolean demandsToSuggestButPerhapsCant(final N ¢) {
    return suggest(¢, null) != null;
  }

  protected abstract String description(N n);

  private Class<N> initializeMyOperandsClass() {
    Class<N> $ = null;
    for (final Method ¢ : getClass().getMethods())
      if (¢.getParameterCount() == 1 && isInstance(¢) && isDefinedHere(¢))
        $ = lowest($, ¢.getParameterTypes()[0]);
    if ($ == null)
      return castClass(ASTNode.class);
    return $;
  }

  private boolean isDefinedHere(final Method ¢) {
    return ¢.getDeclaringClass() == getClass();
  }

  private Class<N> lowest(final Class<N> c1, final Class<?> c2) {
    if (c2 == null)
      return c1;
    if (!ASTNode.class.isAssignableFrom(c2))
      return c1;
    if (c1 == null)
      return castClass(c2);
    if (c1.isAssignableFrom(c2))
      return castClass(c2);
    return c1;
  }

  /** Heuristics to find the class of operands on which this class works.
   * @return a guess for the type of the node. */
  public final Class<N> myAbstractOperandsClass() {
    if (myOperandsClass == null)
      return myOperandsClass = initializeMyOperandsClass();
    return myOperandsClass;
  }

  public Class<N> myActualOperandsClass() {
    final Class<N> $ = myAbstractOperandsClass();
    if (isAbstract($.getModifiers()))
      return null;
    return $;
  }

  public String name() {
    return getClass().getSimpleName();
  }

  public Rewrite suggest(final N ¢) {
    return suggest(¢, null);
  }

  public Rewrite suggest(final N n, final ExclusionManager m) {
    return m != null && m.isExcluded(n) ? null : suggest(n);
  }
}