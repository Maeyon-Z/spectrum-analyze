package com.hit.spectrum.test;

import org.apache.commons.math3.linear.*;


public class OptimizeTest {

    private static double Fx(RealMatrix A, RealVector y, RealVector x, double lambda, double delta){
        double term1 = A.preMultiply(x).subtract(y).getNorm();

        double term2 = 0.0;
        for (double v : x.toArray()) term2 += v;
        term2 *= lambda;

        double term3 = 0;
        for (double v : x.toArray()) term3 += Math.log(v);
        term3 = term3 * (delta / A.getRowDimension());

        return term1 + term2 + term3;
    }

    private static double calculateDelta(RealMatrix A, RealVector y, RealVector x, double lambda) {
        // 计算 ||Ax - y||_2
        RealVector AxMinusY = A.preMultiply(x).subtract(y);
        double term1 = Math.sqrt(AxMinusY.dotProduct(AxMinusY));

        // 计算 λ||x||_1
        double term2 = lambda * x.getL1Norm();

        // 计算 (Ax - y)^T(Ax - y)
        double term3 = AxMinusY.dotProduct(AxMinusY);

        // 计算 (Ax - y)^T y
        double term4 = AxMinusY.dotProduct(y);

        // 计算总和 Δ
        return term1 + term2 + term3 + term4;
    }

    private static RealVector calculateGradient(RealMatrix A, RealVector y, RealVector x, double lambda, double mu) {
        // 计算梯度
        RealVector AxMinusY = A.preMultiply(x).subtract(y);
        return A.operate(AxMinusY).mapMultiply(1.0 / AxMinusY.getNorm()).add(
                x.mapMultiply(lambda)).subtract(x.mapMultiply(mu).ebeDivide(x)
        );
    }

    private static RealMatrix calculateHessian(RealMatrix A, RealVector x) {
        // 计算hessian矩阵
        RealMatrix hessianPhi = A.transpose().preMultiply(A).scalarMultiply(1.0 / x.getNorm());

        // 添加对角块
        RealVector xInv = x.mapMultiply(1.0 / x.getNorm());
        hessianPhi = hessianPhi.add(MatrixUtils.createRealDiagonalMatrix(xInv.toArray()));

        return hessianPhi;
    }

    private static RealVector solveLinearSystem(RealMatrix A, RealVector b) {
        // 使用Apache Commons Math库中的线性系统求解器解线性方程组
        DecompositionSolver solver = new LUDecomposition(A).getSolver();
        return solver.solve(b);
    }

    private static boolean judgeX(double[] x){
        for (double v : x) if (v < 0) return false;
        return true;
    }

    private static boolean judgeAlpha(RealVector x, double alpha, RealVector dx, double c, RealVector gradPhi, RealMatrix A, RealVector y, double delta, double lambda) {

        double term1 = Fx(A, y, x.add(dx.mapMultiply(alpha)), lambda, delta);

        double term2 = Fx(A, y, x, lambda, delta);

        double term3 = gradPhi.dotProduct(dx) * alpha * c;

        return term1 <= term2 + term3;
    }

    public static double[] optimize(double[][] A1, double[] y1){
        y1 = Normalization.normalize(y1);
        for (int i = 0; i < A1.length; i++) {
            A1[i] = Normalization.normalize(A1[i]);
        }

        RealMatrix A = new Array2DRowRealMatrix(A1);
        RealVector y = new ArrayRealVector(y1);
        // 设定初始解x、循环初始次数k、初始步长alpha、步长缩减因子s、控制参数c、最大迭代次数、结束阈值、稀疏因子lambda
        RealVector x = new ArrayRealVector(A.getRowDimension(), 1.0);
        int k = 1;
        double alpha = 1.0, s = 0.5, c = 0.5;
        int maxIterations = 2000000;
        double tolerance = 1e-3;
        double lambda = A.getRowDimension() / 100.0;

        // 开始迭代优化
        while (k < maxIterations){
            // 如果 delta < tolerance 则结束，x为最终结果，否则继续计算
            double delta = calculateDelta(A, y, x, lambda);
            if(delta < tolerance) break;

            // 确定迭代方向
            RealVector gradPhi = calculateGradient(A, y, x, lambda, delta/A.getRowDimension());
            RealMatrix hessianPhi = calculateHessian(A, x);
            RealVector dx = solveLinearSystem(hessianPhi, gradPhi);
            dx.mapMultiplyToSelf(-1.0);
            // 判断步长是否满足要求
            while (!(judgeX(x.toArray()) && judgeAlpha(x, alpha, dx, c, gradPhi, A, y, delta, lambda)))
                alpha *= s;
            x = x.add(dx.mapMultiply(alpha));

            k++;
        }
        return x.toArray();
    }


}