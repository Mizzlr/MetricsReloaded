// MaintainabilityIndexMethod -> MR

// Maintainability Index =
//   MAX(0, (171 - 5.2 * ln(Halstead Volume)
//              - 0.23 * Cyclomatic Complexity
//              - 16.2 * ln(Lines of Code)
//          ) * 100 / 171)


/*
 * Copyright 2005, Sixth and Red River Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

public class MaintainabilityIndexMethodCalculator extends MethodCalculator {
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
            final int complexity = CyclomaticComplexityUtil.calculateComplexity(method,  new PsiElementFilter() {
                @Override
                public boolean isAccepted(PsiElement element) {
                    return !isReducible(element);
                }
            });
            // postMetric(method, complexity);
// ------------------------------------------------------
            if (methodNestingDepth == 0) {
                elementCount = 0;
            }
            methodNestingDepth++;
            elementCount = LineUtil.countLines(method);
            super.visitMethod(method);
            methodNestingDepth--;
            // if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
            //     postMetric(method, elementCount);
            // }
// ------------------------------------------------------
            if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
                final HalsteadVisitor visitor = new HalsteadVisitor();
                method.accept(visitor);
                final double halsteadVolume = visitor.getVolume();
                double mi = Math.max(0.0, (171.0 - 5.2 * Math.log(halsteadVolume + 1e-6)
                    - 0.23 * complexity - 16.2 * Math.log(elementCount + 1e-6)) * 100.0 / 171.0);
                // Maintainability Index =
                //   MAX(0, (171 - 5.2 * ln(Halstead Volume)
                //              - 0.23 * Cyclomatic Complexity
                //              - 16.2 * ln(Lines of Code)
                //          ) * 100 / 171)
                postMetric(method, mi);
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


