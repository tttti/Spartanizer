package il.org.spartan.spartanizer.java;

import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

import org.eclipse.jdt.core.dom.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.java.Environment.*;
import il.org.spartan.spartanizer.utils.*;

/** @author Alex + Dan
 * @since 2016 */
@SuppressWarnings("unused")
public final class EnvironmentVisitor extends ASTVisitor {
  private final LinkedHashSet<Entry<String, Information>> $;
  /** Three groups of visitors here: 1. Non-declarations with a name. 2.
   * Non-declarations without a name. 3. Actual Declarations. First two groups
   * are those in which variable declarations can be made. Since we want to be
   * able to distinguish variables of different scopes, but with, perhaps, equal
   * names, need to keep the scope. The full scope might contain things that do
   * not have a name, hence the need keep to visit ASTNodes without a name such
   * as {@link Block}s, {@link ForStatement}s, etc. Since there can be more than
   * one such node in a parent, they are distinguished by their order of
   * appearance. The third group is the one in which actual addition to the
   * Environment is made. */
  // Holds the current scope full name (Path).
  String scopePath = "";

  EnvironmentVisitor(final LinkedHashSet<Entry<String, Information>> $) {
    this.$ = $;
  }

  /** As of JSL3, AnonymousClassDeclaration's parent can be either
   * ClassInstanceCreation or EnumConstantDeclaration */
  static String anonymousClassDeclarationParentName(final AnonymousClassDeclaration ¢) {
    final ASTNode $ = ¢.getParent();
    if ($ instanceof ClassInstanceCreation)
      return az.classInstanceCreation($).getType() + "";
    assert $ instanceof EnumConstantDeclaration;
    return az.enumConstantDeclaration($).getName() + "";
  }

  Entry<String, Information> convertToEntry(final AnnotationTypeMemberDeclaration ¢) {
    return new MapEntry<>(fullName(¢.getName()), createInformation(¢));
  }

  @SuppressWarnings("hiding") List<Entry<String, Information>> convertToEntry(final FieldDeclaration d) {
    final List<Entry<String, Information>> $ = new ArrayList<>();
    final type t = type.baptize(wizard.condense(d.getType()));
    $.addAll(fragments(d).stream().map(¢ -> new MapEntry<>(fullName(¢.getName()), createInformation(¢, t))).collect(Collectors.toList()));
    return $;
  }

  Entry<String, Information> convertToEntry(final SingleVariableDeclaration ¢) {
    return new MapEntry<>(fullName(¢.getName()), createInformation(¢));
  }

  @SuppressWarnings("hiding") List<Entry<String, Information>> convertToEntry(final VariableDeclarationExpression x) {
    final List<Entry<String, Information>> $ = new ArrayList<>();
    final type t = type.baptize(wizard.condense(x.getType()));
    $.addAll(fragments(x).stream().map(¢ -> new MapEntry<>(fullName(¢.getName()), createInformation(¢, t))).collect(Collectors.toList()));
    return $;
  }

  @SuppressWarnings("hiding") List<Entry<String, Information>> convertToEntry(final VariableDeclarationStatement s) {
    final List<Entry<String, Information>> $ = new ArrayList<>();
    final type t = type.baptize(wizard.condense(s.getType()));
    $.addAll(fragments(s).stream().map(¢ -> new MapEntry<>(fullName(¢.getName()), createInformation(¢, t))).collect(Collectors.toList()));
    return $;
  }

  Information createInformation(final AnnotationTypeMemberDeclaration ¢) {
    return new Information(¢.getParent(), getHidden(fullName(¢.getName())), ¢, type.baptize(wizard.condense(¢.getType())));
  }

  Information createInformation(final SingleVariableDeclaration ¢) {
    return new Information(¢.getParent(), getHidden(fullName(¢.getName())), ¢, type.baptize(wizard.condense(¢.getType())));
  }

  Information createInformation(final VariableDeclarationFragment ¢, final type t) {
    // VariableDeclarationFragment, that comes from either FieldDeclaration,
    // VariableDeclarationStatement or VariableDeclarationExpression,
    // does not contain its type. Hence, the type is sent from the parent in
    // the convertToEntry calls.
    return new Information(¢.getParent(), getHidden(fullName(¢.getName())), ¢, t);
  }

  // Everything besides the actual variable declaration was visited for
  // nameScope reasons. Once their visit is over, the nameScope needs to be
  // restored.
  @Override public void endVisit(final AnnotationTypeDeclaration __) {
    restoreScopeName();
  }

  @Override public void endVisit(final AnonymousClassDeclaration __) {
    restoreScopeName();
  }

  @Override public void endVisit(final Block __) {
    restoreScopeName();
  }

  @Override public void endVisit(final CatchClause __) {
    restoreScopeName();
  }

  @Override public void endVisit(final DoStatement __) {
    restoreScopeName();
  }

  @Override public void endVisit(final EnhancedForStatement __) {
    restoreScopeName();
  }

  @Override public void endVisit(final EnumConstantDeclaration __) {
    restoreScopeName();
  }

  @Override public void endVisit(final EnumDeclaration __) {
    restoreScopeName();
  }

  @Override public void endVisit(final ForStatement __) {
    restoreScopeName();
  }

  @Override public void endVisit(final IfStatement __) {
    restoreScopeName();
  }

  @Override public void endVisit(final MethodDeclaration __) {
    restoreScopeName();
  }

  @Override public void endVisit(final SwitchStatement __) {
    restoreScopeName();
  }

  @Override public void endVisit(final TryStatement __) {
    restoreScopeName();
  }

  @Override public void endVisit(final TypeDeclaration __) {
    restoreScopeName();
  }

  @Override public void endVisit(final WhileStatement __) {
    restoreScopeName();
  }

  @SuppressWarnings("hiding") String fullName(final SimpleName $) {
    return scopePath + "." + $;
  }

  Information get(final LinkedHashSet<Entry<String, Information>> ss, final String s) {
    System.out.println($);
    for (final Entry<String, Information> ¢ : ss)
      if (s.equals(¢.getKey()))
        return ¢.getValue();
    return null;
  }

  /** Returns the {@link Information} of the declaration the current declaration
   * is hiding.
   * @param ¢ the fullName of the declaration.
   * @return The hidden node's Information */
  /* Implementation notes: Should go over result set, and search for declaration
   * which shares the same variable name in the parents. Should return the
   * closest match: for example, if we search for a match to .A.B.C.x, and
   * result set contains .A.B.x and .A.x, we should return .A.B.x.
   *
   * If a result is found in the result set, return said result.
   *
   * To consider: what if said hidden declaration will not appear in
   * 'declaresDown', but will appear in 'declaresUp'? Should we search for it in
   * 'declaresUp' result set? Should we leave the result as it is? I (Dan
   * Greenstein) lean towards searching 'declaresUp'. Current implementation
   * only searches declaresDown.
   *
   * If no match is found, return null. */
  Information getHidden(final String ¢) {
    final String shortName = ¢.substring(¢.lastIndexOf(".") + 1);
    for (String s = parentNameScope(¢); !"".equals(s); s = parentNameScope(s)) {
      final Information i = get($, s + "." + shortName);
      if (i != null)
        return i;
    }
    return null;
  }

  /** Similar to statementOrderAmongTypeInParent, {@link CatchClause}s only */
  static int orderOfCatchInTryParent(final CatchClause c) {
    assert c.getParent() instanceof TryStatement;
    int $ = 0;
    for (final CatchClause ¢ : catchClauses((TryStatement) c.getParent())) {
      if (¢ == c)
        break;
      ++$;
    }
    return $;
  }

  static String parentNameScope(final String ¢) {
    assert "".equals(¢) || ¢.lastIndexOf(".") != -1 : "nameScope malfunction!";
    return "".equals(¢) ? "" : ¢.substring(0, ¢.lastIndexOf("."));
  }

  void restoreScopeName() {
    scopePath = parentNameScope(scopePath);
  }

  /** Order of the searched {@link Statement} in its parent {@link ASTNode},
   * among nodes of the same kind. Zero based.
   * @param s
   * @return The nodes index, according to order of appearance, amongnodesof the
   *         same type. [[SuppressWarningsSpartan]] */
  static int statementOrderAmongTypeInParent(final Statement s) {
    // extract.statements wouldn't work here - we need a shallow extract,
    // not a deep one.
    final ASTNode n = s.getParent();
    if (n == null || !(n instanceof Block) && !(n instanceof SwitchStatement))
      return 0;
    int $ = 0;
    for (final Statement ¢ : n instanceof Block ? statements((Block) n) : statements((SwitchStatement) n)) {
      // This is intentionally '==' and not equals, meaning the exact same
      // Statement,
      // not just equivalence.
      if (¢ == s)
        break;
      if (¢.getNodeType() == s.getNodeType())
        ++$;
    }
    return $;
  }

  @Override public boolean visit(final AnnotationTypeDeclaration ¢) {
    scopePath += "." + ¢.getName();
    return true;
  }

  @Override public boolean visit(final AnnotationTypeMemberDeclaration ¢) {
    $.add(convertToEntry(¢));
    return true;
  }

  @Override public boolean visit(final AnonymousClassDeclaration ¢) {
    scopePath += "." + "#anon_extends_" + anonymousClassDeclarationParentName(¢);
    return true;
  }

  @Override public boolean visit(final Block ¢) {
    scopePath += "." + "#block" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final CatchClause ¢) {
    scopePath += "." + "#catch" + orderOfCatchInTryParent(¢);
    return true;
  }

  @Override public boolean visit(final DoStatement ¢) {
    scopePath += "." + "#do" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final EnhancedForStatement ¢) {
    scopePath += "." + "#enhancedFor" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final EnumConstantDeclaration ¢) {
    scopePath += "." + ¢.getName();
    return true;
  }

  @Override public boolean visit(final EnumDeclaration ¢) {
    scopePath += "." + ¢.getName();
    return true;
  }

  @Override public boolean visit(final FieldDeclaration ¢) {
    $.addAll(convertToEntry(¢));
    return true;
  }

  @Override public boolean visit(final ForStatement ¢) {
    scopePath += "." + "#for" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final IfStatement ¢) {
    scopePath += "." + "#if" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final MethodDeclaration ¢) {
    scopePath += "." + ¢.getName();
    return true;
  }

  @Override public boolean visit(final SingleVariableDeclaration ¢) {
    $.add(convertToEntry(¢));
    return true;
  }

  @Override public boolean visit(final SwitchStatement ¢) {
    scopePath += "." + "#switch" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final TryStatement ¢) {
    scopePath += "." + "#try" + statementOrderAmongTypeInParent(¢);
    return true;
  }

  @Override public boolean visit(final TypeDeclaration ¢) {
    scopePath += "." + ¢.getName();
    return true;
  }

  @Override public boolean visit(final VariableDeclarationExpression ¢) {
    $.addAll(convertToEntry(¢));
    return true;
  }

  @Override public boolean visit(final VariableDeclarationStatement ¢) {
    $.addAll(convertToEntry(¢));
    return true;
  }

  @Override public boolean visit(final WhileStatement ¢) {
    scopePath += "." + "#while" + statementOrderAmongTypeInParent(¢);
    return true;
  }
}