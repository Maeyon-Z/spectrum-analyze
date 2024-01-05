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

public class IdenTest {

    public static void main(String[] args) {
        Api api = new Api();
        List<String> fileNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/mixedData");
        for(int k = 0; k < 10; k++){
            long begin = System.currentTimeMillis();
            String fileName = fileNames.get(k);
            System.out.println("样品: " + fileName);
            String filePath = "/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/mixedData/" + fileName;
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
                String mixedName = jsonObject.getString("name");

                List<String> res = api.iden(yData);
                System.out.println(res);

                System.out.println("时长:" + (System.currentTimeMillis() - begin)/1000 + "秒");
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
