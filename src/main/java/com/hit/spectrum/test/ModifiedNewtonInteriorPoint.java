package com.hit.spectrum.test;
import org.apache.commons.math3.linear.*;

public class ModifiedNewtonInteriorPoint {

    public static RealVector modifiedNewtonInteriorPoint(RealMatrix A, RealVector y) {

        int n = A.getRowDimension();
        double lambda = n / 100.0;

        // 初始化决策变量
        RealVector x = new ArrayRealVector(n, 1.0);

        // 设置迭代次数和收敛条件
        int maxIterations = 1000000;
        double tolerance = 1e-3;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            double delta = calculateDelta(A, y, x, lambda);
            if(delta < tolerance) break;
            double mu = delta / n;
            // 计算拉格朗日函数及其梯度和黑塞矩阵
            RealVector gradPhi = calculateGradient(A, y, x, lambda, mu);
            RealMatrix hessianPhi = calculateHessian(A, x, mu);

            // 使用牛顿法更新决策变量
            RealVector deltaX = solveLinearSystem(hessianPhi, gradPhi);
            x = x.subtract(deltaX);
        }

        return x;
    }

    public static double calculateDelta(RealMatrix A, RealVector y, RealVector x, double lambda) {
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

    private static RealVector calculateGradient(RealMatrix A, RealVector y, RealVector x1, double lambda, double mu) {
        RealVector x = new ArrayRealVector(x1.toArray());
        // 计算拉格朗日函数的梯度
        RealVector AxMinusY = A.preMultiply(x).subtract(y);
        RealVector gradPhi = A.operate(AxMinusY).mapMultiplyToSelf(1.0 / AxMinusY.getNorm()).add(
                x.mapMultiply(lambda)).subtract(x.mapMultiplyToSelf(mu).ebeDivide(x)
        );
        return gradPhi;
    }

    private static RealMatrix calculateHessian(RealMatrix A1, RealVector x1, double mu) {
        RealVector x = new ArrayRealVector(x1.toArray());
        RealMatrix A = new Array2DRowRealMatrix(A1.getData());
        // 计算拉格朗日函数的黑塞矩阵
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

    public static void main(String[] args) {
        // 示例问题：最小化目标函数 ||Ax-y||_2 + λ∑xi - μ∑log(xi)，其中 x >= 0
        RealMatrix A = new Array2DRowRealMatrix(new double[][]{{1, 2}, {2, 3}});
        RealVector y = new ArrayRealVector(new double[]{3, 4});
        double lambda = 0.1;
        double mu = 0.01;

        // 调用修改后的牛顿内点法求解优化问题
        RealVector solution = modifiedNewtonInteriorPoint(A, y);

        // 打印结果
        System.out.println("Optimal solution x: " + solution);
    }
}
