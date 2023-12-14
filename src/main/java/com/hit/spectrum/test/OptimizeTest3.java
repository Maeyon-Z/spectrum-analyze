package com.hit.spectrum.test;

import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class OptimizeTest3 {

    public static double[] optimize(double[][] A1, double[] y1) {
        RealMatrix A = MatrixUtils.createRealMatrix(A1);
        RealVector y = MatrixUtils.createRealVector(y1);

        // 定义模型函数和雅可比矩阵
        MultivariateJacobianFunction model = new MultivariateJacobianFunction() {
            @Override
            public Pair<RealVector, RealMatrix> value(final RealVector point) {
                return new Pair<>(A.preMultiply(point).subtract(y), A.transpose());
            }
        };


        LeastSquaresProblem problem = new LeastSquaresBuilder()
                .start(new double[A1.length])
                .target(new double[A1[0].length])
                .maxEvaluations(200000)
                .maxIterations(200000)
                .model(model)
                .checker(new EvaluationRmsChecker(1e-3))
                .build();

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(GaussNewtonOptimizer.Decomposition.SVD);

        LeastSquaresOptimizer.Optimum optimize = optimizer.optimize(problem);

        return optimize.getPoint().toArray();
    }


}
