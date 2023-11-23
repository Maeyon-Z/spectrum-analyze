package com.hit.spectrum.service;

import com.hit.spectrum.algo.*;
import com.hit.spectrum.config.Params;
import com.hit.spectrum.data.SpectrumData;
import com.hit.spectrum.data.DataConvertUtils;
import com.hit.spectrum.test.TestApiParams;

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
        List<Double> smooth = Whittaker.smooth(Params.lambdaA, data.getCurve(), new ArrayList<>());
        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), Params.ac, Params.width, Params.alpha);
        List<Double> background = Whittaker.smooth(Params.lambdaB, data.getCurve(), peakIds);
        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(data.getCurve(), background);
        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(Params.lambdaA, corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        res.setCurve(sm2);
        return res;
    }

    public static List<Double> savePeak(List<Double> sm2, List<Integer> peakIds){
        List<Double> dbData = new ArrayList<>();
        for (int i = 0; i < sm2.size(); i++){
            if(peakIds.contains(i) && i >= 1 && sm2.get(i-1) < sm2.get(i) && sm2.get(i+1) < sm2.get(i)) dbData.add(sm2.get(i));
            else dbData.add(0.0);
        }
        return dbData;
    }

    public boolean enterStandard(SpectrumData data){
        data = pretreatment(data);
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(data.getCurve()),  Params.ac, Params.width, Params.alpha);
        data.setCurve(savePeak(data.getCurve(), peakIds));
        // todo data入库
        return true;
    }

    /**
     * 混合物组分识别
     */
    public double[] identification(SpectrumData mixedData){
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
