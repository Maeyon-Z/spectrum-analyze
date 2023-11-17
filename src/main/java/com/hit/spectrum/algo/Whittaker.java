package com.hit.spectrum.algo;

import com.hit.spectrum.data.DataConvertUtils;
import org.apache.commons.math3.linear.*;
import java.util.List;

/**
 * Whittaker 平滑器
 */
public class Whittaker {

    private static OpenMapRealMatrix getDifferenceMatrix(Integer n, Integer d){
        OpenMapRealMatrix res = new OpenMapRealMatrix(n-d, n);
        for(int i = 0; i < n-d; i ++){
            res.setEntry(i, i, 1d);
            res.setEntry(i, i + 1, -2d);
            res.setEntry(i, i + 2, 1d);
        }
        return res;
    }

    private static OpenMapRealMatrix getDiagonalMatrix(List<Integer> id, Integer len){
        OpenMapRealMatrix res = new OpenMapRealMatrix(len, len);
        for(int i = 0; i < len; i++){
            if(id.contains(i)){
                res.setEntry(i, i, 0);
            }else res.setEntry(i, i, 1);
        }
        return res;
    }

    public static List<Double> smooth(Double lmd, List<Double> originData, List<Integer> peakIds){
        Integer len = originData.size();
        // 获得单位阵
        OpenMapRealMatrix W = getDiagonalMatrix(peakIds, len);

        // 获得二阶差分矩阵
        OpenMapRealMatrix D = getDifferenceMatrix(len, 2);

        // 计算平滑器
        RealMatrix w = D.transpose().multiply(D).scalarMultiply(lmd).add(W);

        //LU分解求值
        LUDecomposition lu = new LUDecomposition(w);
        RealVector v = new ArrayRealVector(DataConvertUtils.list2Array(originData));
        RealVector res = lu.getSolver().solve(W.operate(v));
        return DataConvertUtils.array2List(res.toArray());
    }
}
