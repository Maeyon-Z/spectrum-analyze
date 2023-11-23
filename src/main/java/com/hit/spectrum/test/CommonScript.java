package com.hit.spectrum.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.hit.spectrum.algo.*;
import com.hit.spectrum.data.DataConvertUtils;
import com.hit.spectrum.data.SpectrumData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommonScript {
    public static List<String> getAllFileNames(String directory) {
        List<String> fileNames = new ArrayList<>();
        try {
            Path path = Paths.get(directory);
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Get only the file name without the path
                    fileNames.add(file.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // Handle the failure
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    public static List<Double> pretreatment(SpectrumData data, TestApiParams params){
        // x轴不变
        // 下面是对y轴的处理
        // 1、初步平滑
        List<Double> smooth = Whittaker.smooth(params.getLambdaA(), data.getCurve(), new ArrayList<>());

        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), params.getAC(), 5, params.getAlpha());
        List<Double> background = Whittaker.smooth(params.getLambdaB(), data.getCurve(), peakIds);

        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(data.getCurve(), background);

        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(params.getLambdaA(), corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        return sm2;
    }

    public static List<DbData> loadDbData(){
        List<String> fileNames = getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData_fix_param_peak");
        List<DbData> res = new ArrayList<>();
//        double[][] res = new double[fileNames.size()][];
        for(String fileName : fileNames){
            try {
                String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData_fix_param_peak/" + fileName;
                InputStream inputStream = Files.newInputStream(Paths.get(filePath));

                // 读取 JSON 文件内容
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                // 使用 FastJSON 解析 JSON 文件
                String jsonContent = new String(bytes, StandardCharsets.UTF_8);
                JSONObject jsonObject = JSONObject.parseObject(jsonContent);

                List<Double> yData = jsonObject.getJSONArray("curve").toJavaList(Double.class);
                String name = jsonObject.getString("name");
                DbData dbData = new DbData(name, yData);
                res.add(dbData);
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return res;
    }

    public static double[] identification(SpectrumData mixedData){
        // 混合物数据
        double[] b =  DataConvertUtils.list2Array(mixedData.getCurve());

        // 标准品数据
        List<DbData> dbDataList = loadDbData();
        double[][] A = new double[dbDataList.size()][];
        for (int i = 0; i < dbDataList.size(); i++) {
            A[i] = DataConvertUtils.list2Array(dbDataList.get(i).getData());
        }

        // 迭代求解 Ax = b
        double[] x0 = OLS.operate(A, b, ((double) A.length)/100.0, false);

        //根据分布筛选疑似物质
        double[] x = Filter.filterByDistribute(x0);

        //通过 T 检验筛选疑似物质
        return Filter.filterByT(x, A, b);
    }

    public static List<Double> savePeak(List<Double> sm2, TestApiParams params){
        List<Integer> peakIds2 = PeakSearch.search(DataConvertUtils.list2Array(sm2), params.getAC(), 5, params.getAlpha());
        List<Double> dbData = new ArrayList<>();
        for (int i = 0; i < sm2.size(); i++){
            if(peakIds2.contains(i) && i >= 1 && sm2.get(i-1) < sm2.get(i) && sm2.get(i+1) < sm2.get(i)) dbData.add(sm2.get(i));
            else dbData.add(0.0);
        }
        return dbData;
    }
}
