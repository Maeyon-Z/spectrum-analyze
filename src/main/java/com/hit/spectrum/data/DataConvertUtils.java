package com.hit.spectrum.data;

import java.util.Arrays;
import java.util.List;

public class DataConvertUtils {

    public static List<Double> array2List(double[] a){
        Double[] t = new Double[a.length];
        for(int i = 0; i < a.length; i ++){
            t[i] = a[i];
        }
        return Arrays.asList(t);
    }

    public static double[] list2Array(List<Double> a){
        double[] res = new double[a.size()];
        for(int i = 0; i < a.size(); i++){
            res[i] = a.get(i);
        }
        return res;
    }
}
