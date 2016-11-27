package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.spartanizer.tippers.TrimmerTestsUtils.*;

import org.junit.*;

@SuppressWarnings("static-method")
public class Issue858 {
  @Ignore public void a() {
    trimmingOf("switch(x){"
        + "case a:"
        + "x=1;"
        + "break;"
        + "case b:"
        + "x=2;break;"
        + "default:"
        + "x=1;"
        + "break;"
        + "}").gives("switch(x){"
            + "case b:"
            + "x=2;break;"
            + "case a:"
            + "default:"
            + "x=1;"
            + "break;"
            + "}");
  }
  
  @Ignore public void b() {
    trimmingOf("switch(x){"
        + "case a:"
        + "x=1;"
        + "break;"
        + "case b:"
        + "x=1;break;"
        + "}").gives("switch(x){"
            + "case a:"
            + "case b:"
            + "x=1;"
            + "break;"
            + "}");
  }
}