package com.hit.spectrum.service;

import com.alibaba.fastjson.JSON;
import com.hit.spectrum.algo.*;
import com.hit.spectrum.config.Params;
import com.hit.spectrum.data.DataConvertUtils;
import com.hit.spectrum.data.SpectrumData;
import com.hit.spectrum.data.SpectrumDb;
import com.hit.spectrum.data.SpectrumDbData;
import com.hit.spectrum.algo.Normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SpectrumService {

    public static boolean enterStandard(String name, List<Double> curve){
        curve = curve.subList(Params.start, Params.end);
        SpectrumDbData res = new SpectrumDbData();
        res.setName(name);
        res.setOrigin(JSON.toJSONString(curve));
        // 1、初步平滑
        List<Double> smooth = Whittaker.smooth(Params.lambdaA, curve, new ArrayList<>());
        res.setSmoothOne(JSON.toJSONString(smooth));
        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), Params.ac, Params.width, Params.alpha);
        List<Double> background = Whittaker.smooth(Params.lambdaB, curve, peakIds);
        res.setBackground(JSON.toJSONString(background));
        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(curve, background);
        res.setCorrected(JSON.toJSONString(corrected));
        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(Params.lambdaA, corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        res.setSmoothTwo(JSON.toJSONString(sm2));
        // 5. 仅保留峰值
        List<Integer> peakIds1 = PeakSearch.search(DataConvertUtils.list2Array(sm2),  Params.ac, Params.width, Params.alpha);
        List<Double> fixPeak = savePeak(sm2, peakIds1);
        res.setFixPeak(JSON.toJSONString(fixPeak));
        // 6. 归一化
        List<Double> norm = DataConvertUtils.array2List(Normalization.normalize(DataConvertUtils.list2Array(fixPeak)));
        res.setNormalized(JSON.toJSONString(norm));
        //入库
        return SpectrumDb.insertData(res);
    }

    private static List<Double> savePeak(List<Double> sm2, List<Integer> peakIds){
        List<Double> dbData = new ArrayList<>();
        for (int i = 0; i < sm2.size(); i++){
            if(peakIds.contains(i)) dbData.add(sm2.get(i));
            else dbData.add(0.0);
        }
        return dbData;
    }

    /**
     * 混合物组分识别
     */
    public static List<String> identification(List<Double> mixedCurve){
        List<String> res = new ArrayList<>();
        // 混合物数据
        double[] b =  pretreatment(mixedCurve);

        // 标准品数据
        List<SpectrumDbData> dbDataList = SpectrumDb.loadDbData();
        double[][] A = new double[dbDataList.size()][];
        for(int i = 0; i < dbDataList.size(); i++){
            A[i] = DataConvertUtils.list2Array(JSON.parseArray(dbDataList.get(i).getNormalized(), Double.class));
        }

        // 迭代求解 Ax = b
        double[] x0 = Optimize.optimize(A, b);

        //根据分布筛选疑似物质
        double[] x = Filter.filterByDistribute(x0);

        //通过 T 检验筛选疑似物质
        double[] ids = Filter.filterByT(x, A, b);

        for(int i = 0; i < ids.length; i++){
            if(ids[i] != 0){
                res.add("  物质：" + dbDataList.get(i).getName() + "，含量：" + ids[i] );
            }
        }
        return res;
    }

    //预处理混合物数据
    private static double[] pretreatment(List<Double> curve){
        curve = curve.subList(Params.start, Params.end);
        // 1、初步平滑
        List<Double> smooth = Whittaker.smooth(Params.lambdaA, curve, new ArrayList<>());
        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), Params.ac, Params.width, Params.alpha);
        List<Double> background = Whittaker.smooth(Params.lambdaB, curve, peakIds);
        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(curve, background);
        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(Params.lambdaA, corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        // 5. 仅保留峰值
        List<Integer> peakIds1 = PeakSearch.search(DataConvertUtils.list2Array(sm2),  Params.ac, Params.width, Params.alpha);
        List<Double> fixPeak = savePeak(sm2, peakIds1);
        // 6. 归一化
        List<Double> norm = DataConvertUtils.array2List(Normalization.normalize(DataConvertUtils.list2Array(fixPeak)));
        return DataConvertUtils.list2Array(norm);
    }

    public static List<SpectrumData> getAll() {
        return SpectrumDb.getAll();
    }

    public static SpectrumData getStandardById(Long id) {
        return SpectrumDb.getStandardById(id);
    }

    public static boolean deleteById(Long id) {
        return SpectrumDb.deleteById(id);
    }
}
