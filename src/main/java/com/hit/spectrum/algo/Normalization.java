package com.hit.spectrum.algo;

public class Normalization {
    // 寻找数组中的最小值
    private static double findMin(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        for (double value : array) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    // 寻找数组中的最大值
    private static double findMax(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (double value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    // 对数组进行归一化
    public static double[] normalize(double[] array) {
        double min = findMin(array);
        double max = findMax(array);
        double[] normalizedArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            normalizedArray[i] = (array[i] - min) / (max - min);
        }
        return normalizedArray;
    }
}
