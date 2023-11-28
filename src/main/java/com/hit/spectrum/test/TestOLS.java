package com.hit.spectrum.test;

import javafx.util.Pair;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;

import java.util.Arrays;

public class TestOLS {

    private static double[] OLS(double[][] A, double[] y, Double lambda) {

        // 初始猜测值
        double[] initialGuess = new double[A.length];

        // 优化器
        CMAESOptimizer optimizer = new CMAESOptimizer(
                1000,
                1e-9,
                true,
                0,
                0,
                null,
                false,
                new SimpleValueChecker(1e-9, 1e-9)
        );


        // 目标函数
        MultivariateFunction objectiveFunction = x -> {
            double reconstructionError = calculateReconstructionError(A, x, y);
            double sparsityTerm = lambda * calculateL1Norm(x);
            return reconstructionError + sparsityTerm;
        };

        // 进行优化
        PointValuePair optimize = optimizer.optimize();

        return optimize.getPoint();
    }

    // 计算重构误差
    private static double calculateReconstructionError(double[][] A, double[] x, double[] y) {
        double[] Ax = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < x.length; j++) {
                Ax[i] += A[i][j] * x[j];
            }
        }

        double error = 0;
        for (int i = 0; i < y.length; i++) {
            error += Math.pow(Ax[i] - y[i], 2);
        }

        return error;
    }

    // 计算L1范数
    private static double calculateL1Norm(double[] x) {
        double norm = 0;
        for (double xi : x) {
            norm += Math.abs(xi);
        }
        return norm;
    }
}
