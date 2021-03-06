package il.org.spartan.athenizer.inflate;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.text.edits.*;

import il.org.spartan.athenizer.inflate.expanders.*;
import il.org.spartan.plugin.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.utils.*;

/** Helper functions for the inflater
 * @author Raviv Rachmiel
 * @since 8-12-16 */
public class InflaterUtilities {
  /** Main function of the application.
   * @param r JD
   * @param nl list of statements and expression in the selected code which for
   *        each we check if we can expand
   * @param g JD
   * @return true iff rewrite object should be applied For now - we only have a
   *         few expanders (1) so we will not infrastructure a full toolbox as
   *         in the spartanizer But we should definitely implement it one day
   *         more expanders should be added to the change ASTNode in a "for
   *         loop" for each expander. SHOULD BE ORGANIZED correctly in a toolbox
   *         infrastracture when we have more expanders */
  static boolean rewrite(final ASTRewrite r, final List<ASTNode> nl, final TextEditGroup __) {
    boolean $ = false;
    if (nl.isEmpty())
      return false;
    for (final ASTNode statement : nl) {
      final ASTNode change = new ReturnTernaryExpander().replacement(az.returnStatement(statement));
      if (change != null) {
        r.replace(statement, change, __);
        $ = true;
      } else {
        final VariableDeclarationStatementSplit s = new VariableDeclarationStatementSplit();
        if (statement instanceof VariableDeclarationStatement && s.canTip(az.variableDeclarationStatement(statement))) {
          s.tip(az.variableDeclarationStatement(statement)).go(r, __);
          $ = true;
        } else {
          final CasesSplit x = new CasesSplit();
          if (statement instanceof SwitchStatement && x.canTip((SwitchStatement) statement)) {
            x.tip((SwitchStatement) statement).go(r, __);
            $ = true;
          } else {
            final DeclarationWithInitExpander s1 = new DeclarationWithInitExpander();
            if (statement instanceof VariableDeclarationStatement && s1.canTip(az.variableDeclarationStatement(statement))) {
              s1.tip(az.variableDeclarationStatement(statement)).go(r, __);
              $ = true;
            }
          }
        }
      }
    }
    return $;
  }

  /* @param u - the WrappedCompilationUnit which is athenized
   *
   * @param ns - the list of statemend which were selected and might be
   * changed */
  static void commitChanges(final WrappedCompilationUnit u, final List<ASTNode> ns) {
    try {
      final TextFileChange textChange = new TextFileChange(u.descriptor.getElementName(), (IFile) u.descriptor.getResource());
      textChange.setTextType("java");
      final ASTRewrite r = ASTRewrite.create(u.compilationUnit.getAST());
      if (rewrite(r, ns, null)) {
        textChange.setEdit(r.rewriteAST());
        if (textChange.getEdit().getLength() != 0)
          textChange.perform(new NullProgressMonitor());
      }
    } catch (final CoreException ¢) {
      monitor.log(¢);
    }
  }

  /* @param u - the WrappedCompilationUnit
   *
   * @return - list of relevant statements for the expanders gets Statements
   * (for now, maybe we should change the name to getASTNodes one day when we
   * have a lot of statements) from compilationUnit - only kind of statements we
   * might need for the athenizer SHOULD BE CHANGED when we add more
   * expanders */
  static List<ASTNode> getStatements(final WrappedCompilationUnit u) {
    final List<ASTNode> $ = new ArrayList<>();
    u.compilationUnit.accept(new ASTVisitor() {
      @Override public boolean visit(final ReturnStatement node) {
        $.add(node);
        return true;
      }

      @Override public boolean visit(final ExpressionStatement node) {
        if (az.assignment(node.getExpression()) != null)
          $.add(node);
        return true;
      }

      @Override public boolean visit(final VariableDeclarationStatement node) {
        $.add(node);
        return true;
      }

      @Override public boolean visit(final SwitchStatement node) {
        $.add(node);
        return true;
      }
    });
    return $;
  }

  /* @param startChar1 - starting char of first interval
   *
   * @param lenth1 - length of first interval
   *
   * @param startChar2 - starting char of second interval
   *
   * @param length2 - length of second interval SPARTANIZED - should use
   * Athenizer one day to understand it */
  static boolean intervalsIntersect(final int startChar1, final int length1, final int startChar2, final int length2) {
    return length1 != 0 && length2 != 0 && (startChar1 < startChar2 ? length1 + startChar1 > startChar2
        : startChar1 != startChar2 ? length2 + startChar2 > startChar1 : length1 > 0 && length2 > 0);
  }

  /* @param ns ASTNodes in compilation unit which might be relevant
   *
   * @return list of selected ASTNodes */
  static List<ASTNode> selectedStatements(final List<ASTNode> ns) {
    final List<ASTNode> $ = new ArrayList<>();
    for (final ASTNode ¢ : ns)
      if (intervalsIntersect(¢.getStartPosition(), ¢.getLength(), Selection.Util.current().textSelection.getOffset(),
          Selection.Util.current().textSelection.getLength()))
        $.add(¢);
    return $;
  }

  public static void aux_go(final CompilationUnit u, final OperationsProvider p) {
    SingleFlater.in(u).from(p).go(ASTRewrite.create(u.getAST()), null);
  }
}
