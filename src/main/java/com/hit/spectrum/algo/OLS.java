package com.hit.spectrum.algo;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
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

    private static double[] getInitialGuess(int m){
        double[] res = new double[m];
        Arrays.fill(res, 0.3);
        return res;
    }

    private static double[] getInitialGuess1(double[] b, double[][] A){
        double[] res0 = new double[A.length];
        Arrays.fill(res0, 0.3);
//        if(A.length > 1) res[1] = 0.9;
        return res0;
//        double max = 0, min = Double.MAX_VALUE;
//        for(int i = 0; i < A.length; i++){
//            res0[i] = 0;
//            for(int j = 0; j < b.length; j++){
//                res0[i] += Math.abs(b[j] - A[i][j]);
//            }
//            max = Math.max(max, res0[i]);
//            min = Math.min(min, res0[i]);
//        }
//        double[] res1 = new double[A.length];
//        for(int i = 0; i < res0.length; i++){
//            res1[i] = res0[i] / max;
//        }
//        double[] res2 = new double[A.length];
//        for(int i = 0; i < res0.length; i++){
//            res2[i] = 1 - res1[i];
//        }
//        return res2;
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

        GradientMultivariateOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE, // 使用 Polak-Ribiere 公式
                new SimpleValueChecker(1e-3, 1e-3) // 设置收敛判据
        );

        double[] init = getInitialGuess1(b, A);

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
