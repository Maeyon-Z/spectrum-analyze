package com.hit.spectrum.algo;

import com.hit.spectrum.data.DataConvertUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.*;

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
        double[] res = new double[b.length];
        System.arraycopy(b, 0, res, 0, b.length);
        double[][] t = new double[i+1][B[0].length];
        System.arraycopy(B, 0, t, 0, i + 1);
//        double[] xt = Optimize.optimize(t, b);
        double[] xt = OLS.operate(t, b, ((double) t.length)/100.0, true);
        for (int j = 0; j < xt.length ; j++) {
            for (int k = 0; k < res.length; k++) {
                res[k] -= xt[j] * B[j][k];
            }
        }
        return res;
    }

    private static double t_Test(double[] sample1, double[] sample2){
        // 创建T检验对象
        TTest tTest = new TTest();
        // 计算P值
        return tTest.tTest(sample1, sample2);
    }

    public static double[] filterByT(double[] x, double[][] A, double[] b){
        LinkedHashMap<Integer, List<Double>> map = getB(x, A);
        double[][] B = new double[map.size()][];
        List<Integer> ids = new ArrayList<>(map.keySet());
        for(int i =  0; i < map.size(); i++){
            B[i] = DataConvertUtils.list2Array(map.get(ids.get(i)));
        }
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
                x[ids.get(i)] = 0;
            }
        }
        return x;
    }

    private static LinkedHashMap<Integer, List<Double>> getB(double[] x, double[][] A) {
        LinkedHashMap<Integer, List<Double>> res = new LinkedHashMap<>();
        Integer[] indices = new Integer[x.length];
        for (int i = 0; i < x.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingDouble((Integer a) -> x[a]).reversed());
        for(int i = 0; i < x.length; i++){
            if(x[indices[i]] == 0.0) continue;
            res.put(indices[i], DataConvertUtils.array2List(A[indices[i]]));
        }
        return res;
    }
}
