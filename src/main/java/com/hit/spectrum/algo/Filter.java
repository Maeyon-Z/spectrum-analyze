package com.hit.spectrum.algo;

import com.hit.spectrum.data.DataConvertUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Filter {
    public static double[] filterByDistribute(double[] x){
        DescriptiveStatistics stats = new DescriptiveStatistics(x);
        double start = stats.getMean() - 2 * stats.getStandardDeviation();
        double end = stats.getMean() + 2 * stats.getStandardDeviation();
        for (int i = 0; i < x.length; i ++)
            if (x[i] > start && x[i] < end) x[i] = 0.0;
        return x;
    }

    private static double[] calcuDelta(int i, double[][] B, double[] b){
        double[][] t = new double[i+1][B[0].length];
        System.arraycopy(B, 0, t, 0, i + 1);
        double[] xt = OLS.operate(t, b, ((double) t.length)/100.0, true);
        for (int j = 0; j < xt.length ; j++) {
            for (int k = 0; k < b.length; k++) {
                b[k] -= xt[j] * B[j][k];
            }
        }
        return b;
    }

    private static double t_Test(double[] sample1, double[] sample2){
        // 创建T检验对象
        TTest tTest = new TTest();
        // 计算P值
        return tTest.tTest(sample1, sample2);
    }

    public static double[] filterByT(double[] x, double[][] A, double[] b){
        double[][] B = getB(x, A);
        double[][] delta = new double[B.length][b.length];

        for (int i = 0; i < B.length; i++) {
            delta[i] = calcuDelta(i, B, b);
        }

        for (int i = 0; i < delta.length; i++) {
            double t;
            if(i == 0){
                t = t_Test(new double[b.length], delta[i]);
            }else {
                t = t_Test(delta[i-1], delta[i]);
            }
            if(t >= 0.01){
                x[i] = 0;
            }
        }
        return x;
    }

    private static double[][] getB(double[] x, double[][] A) {
        List<List<Double>> res = new ArrayList<>();
        Integer[] indices = new Integer[x.length];
        for (int i = 0; i < x.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingDouble((Integer a) -> x[a]).reversed());
        for(int i = 0; i < x.length; i++){
            if(x[indices[i]] == 0.0) break;
            res.add(DataConvertUtils.array2List(A[indices[i]]));
        }

        double[][] array = new double[res.size()][res.get(0).size()];
        for (int i = 0; i < res.size(); i++) {
            List<Double> sublist = res.get(i);
            for (int j = 0; j < res.get(0).size(); j++) {
                array[i][j] = sublist.get(j);
            }
        }
        return array;
    }
}
