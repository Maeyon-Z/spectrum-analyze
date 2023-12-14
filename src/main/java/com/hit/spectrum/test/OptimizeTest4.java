package com.hit.spectrum.test;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.GradientMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.Arrays;


public class OptimizeTest4 {

    public static double[] optimize(double[][] A1, double[] y1) {
        RealMatrix A = MatrixUtils.createRealMatrix(A1);
        RealVector y = MatrixUtils.createRealVector(y1);

        MultivariateFunction objectiveFunction = point -> {
            RealVector x = new ArrayRealVector(point);
            return A.preMultiply(x).subtract(y).getNorm();
        };

        CMAESOptimizer optimizer = new CMAESOptimizer(200000, 1e-3, true, A1.length, 10, new JDKRandomGenerator(), true, new SimpleValueChecker(1e-10, 1e-10));

        double[] lb = new double[A1.length];
        double[] ub = new double[A1.length];
        Arrays.fill(lb, 0.0);
        Arrays.fill(ub, 1.0);

        double[] init = new double[A1.length];
        Arrays.fill(init, 1.0);

        double[] sigma = new double[A1.length];
        Arrays.fill(sigma, 0.5);

        PointValuePair res = optimizer.optimize(
                new ObjectiveFunction(objectiveFunction),
                new SimpleBounds(lb, ub),
                GoalType.MINIMIZE,
                new InitialGuess(init),
                new CMAESOptimizer.PopulationSize((int) (4 + 3 * Math.log(172.0))),
                new CMAESOptimizer.Sigma(sigma),
                new MaxEval(2000000),
                new MaxIter(2000000));

        System.out.println(123);
        return null;
    }


}

