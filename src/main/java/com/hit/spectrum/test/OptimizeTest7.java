package com.hit.spectrum.test;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GradientMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OptimizeTest7 {

    public static double[] optimize(double[][] A1, double[] y1) {
        y1 = Normalization.normalize(y1);
        for (int i = 0; i < A1.length; i++) {
            A1[i] = Normalization.normalize(A1[i]);
        }

        double[] x1 = new double[A1.length];
        Arrays.fill(x1, 1.0);

        List<Integer> l = new ArrayList<>();
        List<Integer> t = new ArrayList<>();
        int iter = 0;

        while(true){
            //remove
            if(x1.length > 10 && iter != 0){
                List<Integer> ids = new ArrayList<>();
                for(int i = 0; i < x1.length; i++){
                    if(x1[i] < 1e-3){
                        ids.add(i);
                        l.add(i);
                    }
                }
                if(ids.size() != 0){
                    t.add(ids.size());
                    A1 = remove2(A1, ids);
                    x1 = remove1(x1, ids);
                }
            }

            Array2DRowRealMatrix A = new Array2DRowRealMatrix(A1);
            RealVector x = new ArrayRealVector(x1);
            RealVector y = new ArrayRealVector(y1);

            double object = getObjectValue(A, x, y);
            if(object < 10) break;
            RealVector grad = getGradient(A, x, y);

            while(check(x, grad) || object <= getObjectValue(A, x.add(grad), y))
                grad.mapMultiplyToSelf(0.9);

            x1 = x.add(grad).toArray();
            iter++;
        }

        List<List<Integer>> ll = new ArrayList<>();
        int begin = 0;
        for(int i = 0; i < t.size(); i++){
            int end = t.get(i) + begin;
            ll.add(l.subList(begin, end));
            begin = end;
        }
        for(int i = ll.size() - 1; i >= 0; i--){
            List<Integer> lll = ll.get(i);
            double[] d = new double[x1.length + lll.size()];
            Arrays.fill(d, 1.0);
            for (int j = 0; j < lll.size(); j++){
                d[lll.get(j)] = 0.0;
            }
            int k = 0, j = 0;
            while(j < x1.length){
                while (d[k] == 0.0) k++;
                d[k] = x1[j];
                k++;
                j++;
            }
            x1 = d;
        }

        return x1;
    }

    private static boolean check(RealVector x, RealVector grad){
        double[] t = x.add(grad).toArray();
        for (double v : t) {
            if (v < 0) return true;
        }
        return false;
    }

    private static double getObjectValue(RealMatrix A, RealVector x, RealVector y){
        return A.preMultiply(x).subtract(y).getNorm();
    }

    private static RealVector getGradient(RealMatrix A, RealVector x, RealVector y){
        return A.transpose().preMultiply(A.preMultiply(x).subtract(y)).mapMultiplyToSelf(-1.0);
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
