package com.hit.spectrum.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PeakSearch {

    private static double[] grad(double[] x, double[] y){
        int n = y.length;
        double[] res = new double[n];

        // 处理边界点
        res[0] = (y[1] - y[0]) / (x[1] - x[0]);
        res[n - 1] = (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]);

        // 计算内部点
        for (int i = 1; i < n - 1; i++) res[i] = (y[i + 1] - y[i - 1]) / (x[i + 1] - x[i - 1]);

        return res;
    }
    private static double[] grad(double[] y){
        int n = y.length;
        double[] res = new double[n];

        // 处理边界点
        res[0] = y[1] - y[0];
        res[n - 1] = y[n - 1] - y[n - 2];

        // 计算内部点
        for (int i = 1; i < n - 1; i++) res[i] = (y[i + 1] - y[i - 1]) / 2;

        return res;
    }

    private static List<Integer> findLocalMaxima(double[] array) {
        List<Integer> localMaxima = new ArrayList<>();

        for (int i = 1; i < array.length - 1; i++) {
            if (array[i] > array[i - 1] && array[i] > array[i + 1]) {
                localMaxima.add(i);
            }
        }

        return localMaxima;
    }

    private static List<Integer> findLocalMinima(double[] array) {
        List<Integer> localMinima = new ArrayList<>();

        for (int i = 1; i < array.length - 1; i++) {
            if (array[i] < array[i - 1] && array[i] < array[i + 1]) {
                localMinima.add(i);
            }
        }
        return localMinima;
    }

    private static double getThreshold(List<Integer> ids, double[] array, double ac){
        double temp = 0.0;

        for(Integer i : ids){
            temp += Math.pow(array[i], 2);
        }
        temp /= ids.size();
        double meanSquare = Math.sqrt(temp);
        return meanSquare * ac;
    }

    private static List<Integer> eliminatingNoise(List<Integer> ids, double[] array, double threshold){
        List<Integer> res = new ArrayList<>();
        for(int i = 0; i < ids.size(); i++){
            if(array[ids.get(i)] < threshold) res.add(ids.get(i));
        }
        return res;
    }

    private static List<Integer> peekSearch(int length, List<Integer> minIds, List<Integer> maxIds, double width, double alpha){
        int[] id = new int[length];
        for(int i : minIds) id[i] = -1;
        for(int i : maxIds) id[i] = 1;
        Set<Integer> res = new HashSet<>();
        for (int i = 0; i < length; i++){
            if(id[i] == -1){
                int peakL = i;
                int peakR = i;
                while (id[peakL] != 1) {
                    peakL -= 1;
                    if (peakL == 0)
                        break;
                }
                while (id[peakR] != 1) {
                    peakR += 1;
                    if (peakR == length - 1)
                        break;
                }
                if (peakL != 0 && peakR != length - 1){
                    int Rpp = peakR - peakL;
                    if(Rpp > width){
                        double Rex = 3 * alpha * Rpp;
                        int half = (int) Rex/2;
                        peakL -= half;
                        peakR += half;
                        for(int j = peakL; j <= peakR; j++) res.add(j);
                    }
                }
            }
        }
        return new ArrayList<>(res);
    }


    public static List<Integer> search(double[] sm1Data, double ac, double width, double alpha){
        // 求二阶导数
        double[] grad = grad(grad(sm1Data));
        // 求二阶导数的所有极大值和极小值对应下标
        List<Integer> minIds = findLocalMinima(grad);
        List<Integer> maxIds = findLocalMaxima(grad);
        // 获取阈值限
        double threshold = getThreshold(minIds, grad, ac);
        // 消除噪声
        minIds = eliminatingNoise(minIds, grad, threshold);

        // 寻峰
        List<Integer> peakIds = peekSearch(sm1Data.length, minIds, maxIds, width, alpha);

        return peakIds;
    }
}
