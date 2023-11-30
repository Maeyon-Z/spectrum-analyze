package com.hit.spectrum.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.hit.spectrum.algo.Filter;
import com.hit.spectrum.algo.OLSTest;
import com.hit.spectrum.data.DataConvertUtils;
import com.hit.spectrum.data.SpectrumData;


import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GenerateInfo {
    private static TestApiParams params = TestApiParams.build(30, 2048, 6.60, 1000.00, -0.1, 0.05);

    private static MiddleInfo identification(SpectrumData mixedData, List<DbData> dbDataList){
        MiddleInfo res = new MiddleInfo();
        // 混合物数据
        double[] b =  DataConvertUtils.list2Array(mixedData.getCurve());

        // 标准品数据
        double[][] A = new double[dbDataList.size()][];
        for (int i = 0; i < dbDataList.size(); i++) {
            A[i] = DataConvertUtils.list2Array(dbDataList.get(i).getData());
        }

        // 迭代求解 Ax = b
        double[] x0 = OLSTest.operate(b, A);
        res.setX0(DataConvertUtils.array2List(x0));
        //根据分布筛选疑似物质
        double[] x1 = Filter.filterByDistribute(x0);
        res.setX1(DataConvertUtils.array2List(x1));

        //通过 T 检验筛选疑似物质
        double[] x2 = Filter.filterByT(x1, A, b);
        res.setX2(DataConvertUtils.array2List(x2));
        return res;
    }

    private static List<Info> testIdentification(){
        List<Info> res = new ArrayList<>();
        List<String> fileNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData_fix_param_peak_2");
        List<DbData> dbDataList = CommonScript.loadDbData();
        for (String fileName : fileNames){
            System.out.println(fileName + " start!");
            Info info = new Info();
            String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData_fix_param_peak_2/" + fileName;
            try {
                InputStream inputStream = Files.newInputStream(Paths.get(filePath));
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                String jsonContent = new String(bytes, StandardCharsets.UTF_8);
                JSONObject jsonObject = JSONObject.parseObject(jsonContent);
                List<Double> xData = jsonObject.getJSONArray("ramanShift").toJavaList(Double.class);
                List<Double> yData = jsonObject.getJSONArray("curve").toJavaList(Double.class);
                String mixedName = jsonObject.getString("name");

                SpectrumData spectrumData = new SpectrumData(xData, yData, mixedName);

                // 2. 组分识别
                MiddleInfo middleInfo = identification(spectrumData, dbDataList);

                info.setX0(middleInfo.getX0());
                info.setX1(middleInfo.getX1());
                info.setX2(middleInfo.getX2());
                info.setTargetName(mixedName);
                info.setResults(new ArrayList<>());
                for(int i = 0; i < middleInfo.getX2().size(); i++){
                    if(middleInfo.getX2().get(i) != 0){
                        info.getResults().add(i + "-" + middleInfo.getX2().get(i) + "-" + fileNames.get(i));
                    }
                }
                res.add(info);

                String jsonString = JSON.toJSONString(res);
                try (FileWriter fileWriter = new FileWriter("result.json")) {
                    fileWriter.write(jsonString);
                    System.out.println("JSON 文件保存成功！");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.println(fileName + " done!");
        }
        return res;
    }

    private static List<Info> testIdentification2(){
        List<Info> res = new ArrayList<>();
        List<String> fileNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/mixedData");
        List<String> dbNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData_fix_param_peak_2");
        List<DbData> dbDataList = CommonScript.loadDbData();
        for (String fileName : fileNames){
            System.out.println(fileName + " start!");
            Info info = new Info();
            String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/mixedData/" + fileName;
            try {
                InputStream inputStream = Files.newInputStream(Paths.get(filePath));
                // 读取 JSON 文件内容
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                // 使用 FastJSON 解析 JSON 文件
                String jsonContent = new String(bytes, StandardCharsets.UTF_8);
                JSONArray jsonArray = JSONArray.parseArray(jsonContent, Feature.OrderedField);

                Object obj = jsonArray.get(0);
                JSONObject jsonObject = (JSONObject) obj;
                List<Double> xData = jsonObject.getJSONArray("raman_shift").toJavaList(Double.class);
                List<Double> yData = jsonObject.getJSONArray("curve").toJavaList(Double.class);
                String mixedName = jsonObject.getJSONArray("name").toJavaList(String.class).toString();
                SpectrumData spectrumData = new SpectrumData();
                spectrumData.setCurve(yData.subList(params.getStart(), params.getEnd()));
                spectrumData.setRamanShift(xData.subList(params.getStart(), params.getEnd()));


                SpectrumData mixedData = new SpectrumData();
                mixedData.setRamanShift(xData.subList(params.getStart(), params.getEnd()));
                mixedData.setName(fileName);
                mixedData.setCurve(CommonScript.savePeak(CommonScript.pretreatment(spectrumData, params), params));

                // 2. 组分识别
                MiddleInfo middleInfo = identification(mixedData, dbDataList);

                info.setX0(middleInfo.getX0());
                info.setX1(middleInfo.getX1());
                info.setX2(middleInfo.getX2());
                info.setTargetName(mixedName);
                info.setResults(new ArrayList<>());
                for(int i = 0; i < middleInfo.getX2().size(); i++){
                    if(middleInfo.getX2().get(i) != 0){
                        info.getResults().add(i + "-" + middleInfo.getX2().get(i) + "-" + dbNames.get(i));
                    }
                }
                res.add(info);

                String jsonString = JSON.toJSONString(res);
                try (FileWriter fileWriter = new FileWriter("result1.json")) {
                    fileWriter.write(jsonString);
                    System.out.println("JSON 文件保存成功！");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.println(fileName + " done!");
        }
        return res;
    }
    public static void main(String[] args) {
        List<Info> res = testIdentification2();
        String jsonString = JSON.toJSONString(res);
        try (FileWriter fileWriter = new FileWriter("result1.json")) {
            fileWriter.write(jsonString);
            System.out.println("JSON 文件保存成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
