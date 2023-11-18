package com.hit.spectrum.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hit.spectrum.data.SpectrumData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class TestScript {

    private static TestApiParams params = TestApiParams.build(100, 2000, 2.00, 1000.00, -0.1, 0.2);

    public static void generateDbData(){
        List<String> fileNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/sampleData");
        for(String fileName : fileNames){
            System.out.println("save " + fileName + " ...");
            try {
                String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/sampleData/" + fileName;
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
                List<Double> yData = jsonObject.getJSONArray("norm_curve").toJavaList(Double.class);

                SpectrumData spectrumData = new SpectrumData();
                spectrumData.setCurve(yData.subList(params.getStart(), params.getEnd()));
                spectrumData.setRamanShift(xData.subList(params.getStart(), params.getEnd()));

                SpectrumData res = new SpectrumData();
                res.setRamanShift(xData.subList(params.getStart(), params.getEnd()));
                res.setName(fileName);
                res.setCurve(CommonScript.pretreatment(spectrumData, params));

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(new File("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbDataNorm/" + fileName), res);

                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.println("save " + fileName + " done...");
        }
    }

    private static void testIdentification(){
        List<String> fileNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/sampleData");
        String fileName = fileNames.get(1);
        String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/sampleData/" + fileName;
        try {
            // 1. 加载混合物数据
            InputStream inputStream = Files.newInputStream(Paths.get(filePath));
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String jsonContent = new String(bytes, StandardCharsets.UTF_8);
            JSONArray jsonArray = JSONArray.parseArray(jsonContent, Feature.OrderedField);
            Object obj = jsonArray.get(0);
            JSONObject jsonObject = (JSONObject) obj;
            List<Double> xData = jsonObject.getJSONArray("raman_shift").toJavaList(Double.class);
            List<Double> yData = jsonObject.getJSONArray("curve").toJavaList(Double.class);
//            List<String> mixedName = jsonObject.getJSONArray("name").toJavaList(String.class);
            String mixedName = jsonObject.getString("name");

            SpectrumData spectrumData = new SpectrumData(xData.subList(params.getStart(), params.getEnd()),
                    yData.subList(params.getStart(), params.getEnd()), mixedName);

            SpectrumData mixedData = new SpectrumData(xData.subList(params.getStart(), params.getEnd()),
                    CommonScript.pretreatment(spectrumData, params), mixedName);

            // 2. 组分识别
            double[] res = CommonScript.identification(mixedData);

            List<String> dbNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData");
            for(int i = 0; i < res.length; i++){
                if(res[i] != 0){
                    System.out.println(i + "-" + res[i] + "-" + dbNames.get(i));
                }
            }
            System.out.println(mixedName);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testIdentification();
    }

}
