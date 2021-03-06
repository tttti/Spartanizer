package il.org.spartan.spartanizer.utils.tdd;

import org.junit.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;

/** @author David Cohen
 * @author Shahar Yair
 * @author Zahi Mizrahi
 * @since 16-11-6 */
@SuppressWarnings("static-method")
public class Issue710 {
  @Test public void test01() {
    assert !determineIf.returnsNull(null);
  }

  @Test public void test02() {
    assert determineIf.returnsNull(az.methodDeclaration(wizard.ast("static void f() { return null;}")));
  }

  @Test public void test03() {
    assert !determineIf.returnsNull(az.methodDeclaration(wizard.ast("static int g() { return 2;}")));
  }

  @Test public void test04() {
    assert !determineIf.returnsNull(az.methodDeclaration(wizard.ast("static boolean h() { return true;}")));
  }

  @Test public void test05() {
    assert !determineIf.returnsNull(az.methodDeclaration(wizard.ast("static boolean h() { int null1=2; return true;}")));
  }

  @Test public void test06() {
    assert !determineIf.returnsNull(az.methodDeclaration(wizard.ast("static String h() { return \"return null;\"; }")));
  }

  @Test public void test07() {
    assert !determineIf
        .returnsNull(az.methodDeclaration(wizard.ast("Object f() {return new Object() {@Override public String toString() {return null;}};}")));
  }

  @Test public void test08() {
    assert !determineIf.returnsNull(az.methodDeclaration(wizard.ast("Supplier<Integer> f() {" + " return () -> {" + " return null; }; " + "}")));
  }
}