// ProgrammerEfficiencyMethod -> MR
// http://bvicam.ac.in/bjit/downloads/pdf/issue6/09.pdf
// Efficiency metric is defined as:
//     E(Prog) = SQRT(F(c) x LOC(d) x P(s) x T(c) x E)
// Where,
//     E(Prog) the efficiency of a programmer in a project (here for a method).
//     F(c)    the function complexity
//     LOC(d)  the lines of code developed for assigned function.
//     P(S)    the programmer’s status.
//     T(c)    the total time consumed (in minutes) for developing the Lines of code.
//     E       the efficiency constant.

  // Programmers Status --> (log10(Lines of code contributed to code base) - 1)
  //           1 Fresher
  //           2 Intermediate
  //           3 Experienced
  //    Function complexity --> (CC of method) / 5
  //           1 Low (CyclomaticComplexity of method < 5)
  //           2 Medium (5 <= CC < 10)
  //           3 High (CC > 10)

// T(c) --> (Halstead Program Time of that method) / 60
// Efficiency Constant 100 % calculator
// MaintainabilityIndexMethod -> MR

package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.sixrr.stockmetrics.utils.LineUtil;
import com.sixrr.metrics.utils.MethodUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiElementFilter;
import com.sixrr.stockmetrics.halstead.HalsteadVisitor;
import com.sixrr.stockmetrics.utils.CyclomaticComplexityUtil;

public class ProgrammerEfficiencyMethodCalculator extends MethodCalculator {
    private int methodNestingDepth = 0;
    private int elementCount = 0;

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitMethod(PsiMethod method) {
            if (MethodUtils.isAbstract(method)) {
                return;
            }
            final int complexity = CyclomaticComplexityUtil.calculateComplexity(method,
                new PsiElementFilter() {
                @Override
                public boolean isAccepted(PsiElement element) {
                    return !isReducible(element);
                }
            });

            if (methodNestingDepth == 0) {
                elementCount = 0;
            }
            methodNestingDepth++;
            elementCount = LineUtil.countLines(method);
            super.visitMethod(method);
            methodNestingDepth--;

            if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
                final HalsteadVisitor visitor = new HalsteadVisitor();
                method.accept(visitor);

                double functionComplexity = (double) complexity / 5.0;
                double programmerStatus =  2.0; // TODO: Programmers Status --> (log10(Lines of code contributed to code base) - 1)
                double efficiencyConstant = 100.0; // Efficiency Constant 100 % calculator
                double linesOfCode = elementCount; // LOC(d)  the lines of code developed for assigned function.
                double programTimeMethod = (double) visitor.getProgramTime() / 60.0; // T(c) --> (Halstead Program Time of that method) / 60

                // E(Prog) = SQRT(F(c) x LOC(d) x P(s) x T(c) x E)
                double programmerEfficiencyMethod = Math.min(Math.sqrt(functionComplexity
                    * linesOfCode * programmerStatus * programTimeMethod * efficiencyConstant), 100);

                postMetric(method, programmerEfficiencyMethod);
            }
            methodNestingDepth++;
            super.visitMethod(method);
            methodNestingDepth--;
        }
    }

    public boolean isReducible(PsiElement element) {
        return false;
    }
}


