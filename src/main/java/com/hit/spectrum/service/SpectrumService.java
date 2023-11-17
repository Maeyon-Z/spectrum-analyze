package com.hit.spectrum.service;

import com.hit.spectrum.algo.*;
import com.hit.spectrum.data.SpectrumData;
import com.hit.spectrum.data.DataConvertUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SpectrumService {

    private static SpectrumData pretreatment(SpectrumData data){
        SpectrumData res = new SpectrumData();
        // x轴不变
        res.setRamanShift(data.getRamanShift());
        // 下面是对y轴的处理
        // 1、初步平滑
        List<Double> smooth = Whittaker.smooth(2.0, data.getCurve(), new ArrayList<>());
        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), -0.1, 5, 0.2);
        List<Double> background = Whittaker.smooth(1000.0, data.getCurve(), peakIds);
        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(data.getCurve(), background);
        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(2.0, corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        res.setCurve(sm2);
        return res;
    }

    public boolean enterStandard(SpectrumData data){
        data = pretreatment(data);
        // todo data入库
        return true;
    }


    /**
     * 混合物组分识别
     */
    public double[] identification(SpectrumData mixedData){
        mixedData = pretreatment(mixedData);

        // 混合物数据
        double[] b =  DataConvertUtils.list2Array(mixedData.getCurve());

        // 标准品数据 todo 从数据库获取
        double[][] A = new double[1][1];

        // 迭代求解 Ax = b
        double[] x0 = OLS.operate(A, b, ((double) A.length)/100.0, false);

        //根据分布筛选疑似物质
        double[] x = Filter.filterByDistribute(x0);

        //通过 T 检验筛选疑似物质
        return Filter.filterByT(x, A, b);

    }
}
