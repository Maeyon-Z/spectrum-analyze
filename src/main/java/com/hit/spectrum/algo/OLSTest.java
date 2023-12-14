package com.hit.spectrum.algo;

import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.nonlinear.scalar.*;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;

import java.util.Arrays;
import java.util.function.Function;

public class OLSTest {

    public static double[] operate(double[] observedValues, double[][] designMatrix){

        // 定义非负最小二乘问题的目标函数
        Function<double[], Double> objectiveFunction = x -> {
            double result = 0.0;
            for (int i = 0; i < observedValues.length; i++) {
                double prediction = 0.0;
                for (int j = 0; j < x.length; j++) {
                    prediction += designMatrix[j][i] * x[j];
                }
                result += Math.pow(observedValues[i] - prediction, 2);
            }
            return result;
        };

        // 设置非负约束
        double[] lb = new double[designMatrix.length];
        Arrays.fill(lb, 0.0);
        double[] ub = new double[designMatrix.length];
        Arrays.fill(ub, 1.0);
        SimpleBounds simpleBounds = new SimpleBounds(lb, ub);

        // 创建优化器
        MultivariateOptimizer optimizer = new PowellOptimizer(1e-3, 1e-3);

        // 设置优化目标
        ObjectiveFunction objective = new ObjectiveFunction(x -> objectiveFunction.apply(x));
        // 设置初始点
        double[] initialGuess = new double[designMatrix.length];
        Arrays.fill(initialGuess, 0.0);

        // 运行优化算法
        PointValuePair result = optimizer.optimize(objective, GoalType.MINIMIZE, new InitialGuess(initialGuess), new MaxEval(2000000), simpleBounds);

        // 输出最优参数
        return result.getPoint();
    }

}

