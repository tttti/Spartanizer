package il.org.spartan.spartanizer.research.util;

import java.text.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.formatter.*;
import org.eclipse.jface.text.*;
import org.eclipse.text.edits.*;

/** @author Ori Marcovitch
 * @since Nov 13, 2016 */
public class format {
  public static String code(final String code) {
    final TextEdit textEdit = ToolFactory.createCodeFormatter(null).format(CodeFormatter.K_UNKNOWN, code, 0, code.length(), 0, null);
    final IDocument $ = new Document(code);
    try {
      if (textEdit != null)
        textEdit.apply($);
    } catch (final BadLocationException | MalformedTreeException ¢) {
      ¢.printStackTrace();
    }
    return $.get();
  }

  static final NumberFormat numberFormatter = new DecimalFormat("#0.00");

  public static String decimal(final double ¢) {
    return numberFormatter.format(¢);
  }
}
