package com.hit.spectrum.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.hit.spectrum.Api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class InputTest {

    public static void main(String[] args) {
        Api api = new Api();
        List<String> fileNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/sampleData");
        for(int k = 0; k < fileNames.size(); k++){
            String fileName = fileNames.get(k);
            String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/sampleData/" + fileName;
            fileName = fileName.substring(0, fileName.indexOf('.'));
            System.out.println(fileName + "开始入库");
            try {
                // 1. 加载混合物数据
                InputStream inputStream = Files.newInputStream(Paths.get(filePath));
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                String jsonContent = new String(bytes, StandardCharsets.UTF_8);
                JSONArray jsonArray = JSONArray.parseArray(jsonContent, Feature.OrderedField);
                Object obj = jsonArray.get(0);
                JSONObject jsonObject = (JSONObject) obj;
                List<Double> yData = jsonObject.getJSONArray("curve").toJavaList(Double.class);
                api.input(fileName, yData);
                System.out.println(fileName + "入库成功");
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
