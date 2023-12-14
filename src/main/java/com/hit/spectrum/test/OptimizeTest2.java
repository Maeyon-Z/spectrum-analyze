package com.hit.spectrum.test;

import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OptimizeTest2 {

    private static boolean judgeX(double[] x){
        for (double v : x) if (v < 0) return false;
        return true;
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

            System.out.println(k);
            alpha = 1.0;

            double f1 = A.preMultiply(x).subtract(y).getNorm();

            if(f1 < tolerance) break;

            // 确定迭代方向
            RealVector dx = A.transpose().preMultiply(A.preMultiply(x).subtract(y)).mapDivide(f1);

            dx.mapMultiplyToSelf(-1.0);
            // 判断步长是否满足要求
            while (!(judgeX(x.add(dx.mapMultiply(alpha)).toArray()) && f1 > A.preMultiply(x.add(dx.mapMultiply(alpha))).subtract(y).getNorm()))
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

}
