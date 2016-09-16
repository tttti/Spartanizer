package il.org.spartan.spartanizer.wring.dispatch;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

/** Determines whether an {@link ASTNode} is spartanization disabled. In the
 * current implementation, only instances of {@link BodyDeclaration} may be
 * disabled, and only via their {@link Javadoc} comment
 * @author Ori Roth
 * @since 2016/05/13 */
public class DisabledChecker {
  private class BodyDeclarationVisitor extends ASTVisitor {
    // TODO: Ori Roth: Don't use short names for global things.
    final Set<ASTNode> dns;
    final Set<ASTNode> ens;

    BodyDeclarationVisitor(final Set<ASTNode> dns, final Set<ASTNode> ens) {
      this.dns = dns;
      this.ens = ens;
    }

    private boolean go(final BodyDeclaration ¢) {
      return go(¢, ¢.getJavadoc());
    }

    public boolean go(final BodyDeclaration d, final Javadoc j) {
      return j == null || go(d, j + "");
    }

    public boolean go(final BodyDeclaration d, final String s) {
      insertAnnotated(d, s, dns, disablers);
      insertAnnotated(d, s, ens, enablers);
      return true;
    }

    private void insertAnnotated(final BodyDeclaration d, final String s, final Set<ASTNode> g, final String[] ids) {
      for (final String id : ids)
        if (s.contains(id)) {
          g.add(d);
          return;
        }
    }

    @Override public boolean visit(final AnnotationTypeDeclaration ¢) {
      return go(¢);
    }

    @Override public boolean visit(final AnnotationTypeMemberDeclaration ¢) {
      return go(¢);
    }

    @Override public boolean visit(final EnumConstantDeclaration ¢) {
      return go(¢);
    }

    @Override public boolean visit(final EnumDeclaration ¢) {
      return go(¢);
    }

    @Override public boolean visit(final FieldDeclaration ¢) {
      return go(¢);
    }

    @Override public boolean visit(final Initializer ¢) {
      return go(¢);
    }

    @Override public boolean visit(final MethodDeclaration ¢) {
      return go(¢);
    }

    @Override public boolean visit(final TypeDeclaration ¢) {
      return go(¢);
    }
  }

  /** Disable spartanization identifier, used by the programmer to indicate a
   * method/class not to be spartanized */
  public static final String disablers[] = { "[[Hedonistic]]", "@DisableSpartan", "Hedonistic", "[[hedoni]]", "[[hedonisti]]", "[[hedon]]",
      "[[hedo]]" };
  /** Enable spartanization identifier, used by the programmer to indicate a
   * method/class to be spartanized */
  public static final String enablers[] = { "[[Spartan]]", "@EnableSpartan", "[[Spartan]]", "[[spartan]]", "[[sparta]]" };
  final Set<ASTNode> disabledNodes;
  final Set<ASTNode> enabledNodes;

  public DisabledChecker(final CompilationUnit u) {
    disabledNodes = new HashSet<>();
    enabledNodes = new HashSet<>();
    if (u == null)
      return;
    u.accept(new BodyDeclarationVisitor(disabledNodes, enabledNodes));
  }

  /** @param n node
   * @return true iff spartanization is disabled for n */
  public boolean check(final ASTNode n) {
    for (ASTNode p = n; p != null; p = p.getParent()) {
      if (disabledNodes.contains(p))
        return true;
      if (enabledNodes.contains(p))
        break;
    }
    return false;
  }
}