package com.hit.spectrum.algo;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.GradientMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OLS {
    private static LinearConstraintSet getNonnegativityConstraints(int m){
        List<LinearConstraint> res = new ArrayList<>();
        for(int i = 0; i < m; i++){
            double[] d = new double[m];
            d[i] = 1;
            res.add(new LinearConstraint(d, Relationship.GEQ, 0.0d));
        }
        return new LinearConstraintSet(res);
    }

    private static double[] getInitialGuess(double[] b, double[][] A){
        double[] res = new double[A.length];
        Arrays.fill(res, 1);
        return res;
    }

    private static MultivariateFunction getObjectiveFunction(RealMatrix matrixA, RealVector vectorB, double lmd, boolean isIter){
        MultivariateFunction objectiveFunction;
        if(isIter){
            objectiveFunction = point -> {
                RealVector x = new ArrayRealVector(point);
                RealVector Ax = matrixA.preMultiply(x);
                return Ax.subtract(vectorB).getNorm();
            };
        }else {
            objectiveFunction = point -> {
                RealVector x = new ArrayRealVector(point);
                RealVector Ax = matrixA.preMultiply(x);
                double norm2 = Ax.subtract(vectorB).getNorm();
                double norm1 = x.getL1Norm();
                return norm2 + lmd * norm1;
            };
        }
        return objectiveFunction;
    }

    private static MultivariateVectorFunction getGradFunction(RealMatrix matrixA, RealVector vectorB, double lmd, boolean isIter){
        MultivariateVectorFunction gradientFunction;
        if(isIter){
            gradientFunction = point -> {
                RealVector x = new ArrayRealVector(point);
                RealVector Ax = matrixA.preMultiply(x);
                RealVector residual = Ax.subtract(vectorB);
                RealVector gradient = matrixA.operate(residual);
                return gradient.toArray();
            };
        }else {
            gradientFunction = point -> {
                RealVector x = new ArrayRealVector(point);
                RealVector Ax = matrixA.preMultiply(x);
                RealVector residual = Ax.subtract(vectorB);
                RealVector gradient = matrixA.operate(residual);
                for (int i = 0; i < point.length; i++) {
                    if (point[i] < 0) {
                        gradient.setEntry(i, gradient.getEntry(i) - lmd);
                    } else if (point[i] > 0) {
                        gradient.setEntry(i, gradient.getEntry(i) + lmd);
                    }
                }
                return gradient.toArray();
            };
        }
        return gradientFunction;
    }

    public static double[] operate(double[][] A, double[] b, double lmd, boolean isIter){
        RealMatrix matrixA = new Array2DRowRealMatrix(A);
        RealVector vectorB = new ArrayRealVector(b);

        // 定义目标函数
        MultivariateFunction objectiveFunction = getObjectiveFunction(matrixA, vectorB, lmd, isIter);
        // 定义目标函数的梯度
        MultivariateVectorFunction gradientFunction = getGradFunction(matrixA, vectorB, lmd, isIter);

        // 设置非负约束
        double[] lb = new double[A.length];
        Arrays.fill(lb, 0.0);
        double[] ub = new double[A.length];
        Arrays.fill(ub, 1.0);
        SimpleBounds simpleBounds = new SimpleBounds(lb, ub);

        GradientMultivariateOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE, // 使用 Polak-Ribiere 公式
                new SimpleValueChecker(1e-3, 1e-3) // 设置收敛判据
        );

        double[] init = getInitialGuess(b, A);

        PointValuePair res = optimizer.optimize(
                new MaxEval(2000000), // 最大迭代次数
                new ObjectiveFunction(objectiveFunction),
                new ObjectiveFunctionGradient(gradientFunction), // 应用梯度函数
                getNonnegativityConstraints(A.length),//通过线性约束的形式定义非负约束
                GoalType.MINIMIZE,
                new InitialGuess(init) // 设置初始猜测
        );

        return res.getPoint();
    }

}
