package com.hit.spectrum.test;

import com.hit.spectrum.algo.Normalization;
import org.apache.commons.math3.linear.*;

import java.util.*;


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
        double term1 = AxMinusY.getNorm();

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
        RealVector term1 = A.transpose().preMultiply(A.preMultiply(x).subtract(y));

        double[] x1 = new double[x.toArray().length];
        Arrays.fill(x1, lambda);
        RealVector term2 = new ArrayRealVector(x1);

        x1 = x.toArray();
        for (int i = 0; i < x1.length; i++) x1[i] = mu / x1[i];
        RealVector term3 = new ArrayRealVector(x1);

        return term1.add(term2).subtract(term3);
    }

    private static RealMatrix calculateHessian(RealMatrix A, RealVector x, double mu) {
        RealMatrix term1 = A.multiply(A.transpose());

        double[] x1 = x.toArray();
        for (int i = 0; i < x1.length; i++) x1[i] = -1 * mu / Math.sqrt(x1[i]);
        RealMatrix term2 = MatrixUtils.createRealDiagonalMatrix(x1);

        return term1.add(term2);
    }

    private static RealVector solveLinearSystem(RealMatrix A, RealVector b) {
        RealMatrix inverse = new LUDecomposition(A).getSolver().getInverse();
        return inverse.preMultiply(b);
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

        //
        List<Integer> l = new ArrayList<>();

        // 开始迭代优化
        while (k < maxIterations){
            //remove
            double[] xTemp = x.toArray();
            double[][] ATemp = A.getData();
            List<Integer> ids = new ArrayList<>();
            for(int i = 0; i < xTemp.length; i++){
                if(xTemp[i] < 1e-5){
                    if(i == 0){
                        System.out.println(123);
                    }
                    ids.add(i);
                    l.add(i);
                }
            }
            if(ids.size() != 0){
                A = new Array2DRowRealMatrix(remove2(ATemp, ids));
                x = new ArrayRealVector(remove1(xTemp, ids));
            }

            alpha = 1.0;
            // 如果 delta < tolerance 则结束，x为最终结果，否则继续计算
            double delta = calculateDelta(A, y, x, lambda);
            if(delta < tolerance) break;

            // 确定迭代方向
            RealVector gradPhi = calculateGradient(A, y, x, lambda, delta/A.getRowDimension());
            RealMatrix hessianPhi = calculateHessian(A, x, delta/A.getRowDimension());
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

    private static double[][] remove2(double[][] aTemp, List<Integer> ids) {
        double[][] res = new double[aTemp.length - ids.size()][aTemp[0].length];
        int k = 0;
        for(int i = 0; i < aTemp.length; i++){
            if(!ids.contains(i))
                res[k++] = aTemp[i];
        }
        return res;
    }

    private static double[] remove1(double[] xTemp, List<Integer> ids) {
        double[] res = new double[xTemp.length - ids.size()];
        int k = 0;
        for(int i = 0; i < xTemp.length; i++){
            if(!ids.contains(i))
                res[k++] = xTemp[i];
        }
        return res;
    }

    private static double findMin(double[] x) {
        double min = x[0];
        for (int i = 1; i < x.length; i++) {
            if(x[i] < min) min = x[i];
        }
        return min;
    }


    public static double[] optimize2(double[][] A1, double[] y1) {
        RealMatrix A = new Array2DRowRealMatrix(A1);
        RealVector y = new ArrayRealVector(y1);
        SingularValueDecomposition svd = new SingularValueDecomposition(A);
        RealMatrix pseudoInverse = svd.getSolver().getInverse();

        // 计算最小二乘解
        RealVector x = pseudoInverse.preMultiply(y);
        return x.toArray();
    }
}
