package il.org.spartan.spartanizer.java;

import static il.org.spartan.Utils.*;
import static il.org.spartan.spartanizer.ast.step.*;
import static il.org.spartan.spartanizer.engine.type.*;
import static il.org.spartan.spartanizer.engine.type.Primitive.Certain.*;
import static il.org.spartan.spartanizer.engine.type.Primitive.Uncertain.*;
import static org.eclipse.jdt.core.dom.ASTNode.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.ast.*;

/** @author Yossi Gil
 * @since 2016 */
public enum stringType {
  ;
  /** @param x JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an expression
   *         whose type is provably not of type {@link String}, in the sense
   *         used in applying the <code>+</code> operator to concatenate
   *         strings. concatenation. */
  public static boolean isNot(final Expression ¢) {
    return stringType.isNotFromContext(¢) || !in(get(¢), STRING, ALPHANUMERIC);
  }

  private static boolean isNotFromContext(final Expression x) {
    for (ASTNode context = parent(x); context != null; context = parent(context)) {
      if (iz.infixPlus(context))
        continue;
      // TODO: Niv, you can make this switch simpler by using function {@link
      // iz.is}
      // TODO: Yossi, this entire method will probably be removed soon.
      // the !in(get(¢), STRING, ALPHANUMERIC) should cover this as well, but
      // it fails one test right now so I haven't changed it yet
      switch (context.getNodeType()) {
        case INFIX_EXPRESSION:
          return true;
        case ARRAY_ACCESS:
        case PREFIX_EXPRESSION:
        case POSTFIX_EXPRESSION:
          return true;
        case PARENTHESIZED_EXPRESSION:
          continue;
        default:
          return false;
      }
    }
    return false;
  }
}
