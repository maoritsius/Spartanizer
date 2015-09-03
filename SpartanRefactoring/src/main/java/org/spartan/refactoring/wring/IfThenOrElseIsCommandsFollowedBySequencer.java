package org.spartan.refactoring.wring;

import static org.spartan.refactoring.wring.Wrings.*;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Restructure.duplicateInto;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>if (x) {
 *   ;
 *   f();
 *   return a;
 * } else {
 *   ;
 *   g();
 *   {
 *   }
 * }</code> into <code>if (x) {
 *   f();
 *   return a;
 * }
 * g();</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfThenOrElseIsCommandsFollowedBySequencer extends Wring<IfStatement> {
  static boolean endsWithSequencer(final Statement s) {
    return Is.sequencer(Extract.lastStatement(s));
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Remove redundant else (possibly after inverting if statement)";
  }
  @Override boolean eligible(@SuppressWarnings("unused") final IfStatement _) {
    return true;
  }
  @Override Rewrite make(final IfStatement s) {
    return new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        assert scopeIncludes(s);
        final IfStatement shorterIf = makeShorterIf(s);
        final List<Statement> remainder = Extract.statements(elze(shorterIf));
        shorterIf.setElseStatement(null);
        final Block parent = asBlock(s.getParent());
        final Block newParent = s.getAST().newBlock();
        if (parent != null) {
          addAllReplacing(newParent.statements(), parent.statements(), s, shorterIf, remainder);
          r.replace(parent, newParent, g);
        } else {
          newParent.statements().add(shorterIf);
          duplicateInto(remainder, newParent.statements());
          r.replace(s, newParent, g);
        }
      }
    };
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return elze(s) != null && (endsWithSequencer(then(s)) || endsWithSequencer(elze(s)));
  }
}