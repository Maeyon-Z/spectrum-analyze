package com.hit.spectrum.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.hit.spectrum.algo.Calibration;
import com.hit.spectrum.algo.PeakSearch;
import com.hit.spectrum.algo.Whittaker;
import com.hit.spectrum.data.DataConvertUtils;
import com.hit.spectrum.data.SpectrumData;
import com.hit.spectrum.test.TestApiParams;
import com.hit.spectrum.test.TestApiRes;
import com.hit.spectrum.service.SpectrumService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/spectrum")
public class TestApi {

    @GetMapping("/getAllData/{type}")
    public List<SampleName> getAllData(@PathVariable("type") Integer type){
        List<SampleName> res = new ArrayList<>();
        List<String> fileNames = getAllFileName(type);
        for (int i = 0; i < fileNames.size(); i++){
            res.add(new SampleName(i, fileNames.get(i)));
        }
        return res;
    }

    @PostMapping("/test/{type}")
    public TestApiRes test(@RequestBody TestApiParams params, @PathVariable("type") Integer type){
        String fileName = params.getFileName();
        TestApiRes res = new TestApiRes();
        try {
            String filePath = type == 1 ? "sampleData/" + fileName : "mixedData/" + fileName;
            ClassPathResource classPathResource = new ClassPathResource(filePath);
            InputStream inputStream = classPathResource.getInputStream();

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
            xData = xData.subList(params.getStart(), params.getEnd());
            yData = yData.subList(params.getStart(), params.getEnd());
            SpectrumData spectrumData = new SpectrumData();
            spectrumData.setCurve(yData);
            spectrumData.setRamanShift(xData);
            res = pretreatment(spectrumData, params);
            inputStream.close();

            if(type == 1){
                String filePath1 = "dbData_fix_param_peak_2/" + fileName;
                ClassPathResource classPathResource1 = new ClassPathResource(filePath1);
                InputStream inputStream1 = classPathResource1.getInputStream();

                // 读取 JSON 文件内容
                byte[] bytes1 = new byte[inputStream1.available()];
                inputStream1.read(bytes1);
                // 使用 FastJSON 解析 JSON 文件
                String jsonContent1 = new String(bytes1, StandardCharsets.UTF_8);
                JSONObject jsonObject1 = JSONObject.parseObject(jsonContent1);
                res.setDbData(jsonObject1.getJSONArray("curve").toJavaList(Double.class));
                inputStream1.close();
            }else{
                res.setDbData(CommonScript.savePeak(res.getSm2(), params));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

    @PostMapping("/iden")
    public TestApiRes iden(@RequestBody TestApiParams params){
        String fileName = params.getFileName();
        TestApiRes res = new TestApiRes();
        try {
            String filePath = "mixedData/" + fileName;
            ClassPathResource classPathResource = new ClassPathResource(filePath);
            InputStream inputStream = classPathResource.getInputStream();

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
            xData = xData.subList(params.getStart(), params.getEnd());
            yData = yData.subList(params.getStart(), params.getEnd());
            SpectrumData spectrumData = new SpectrumData();
            spectrumData.setCurve(yData);
            spectrumData.setRamanShift(xData);
            res = pretreatment(spectrumData, params);
            inputStream.close();

            res.setDbData(CommonScript.savePeak(res.getSm2(), params));

            spectrumData.setCurve(res.getDbData());
            spectrumData.setRamanShift(xData);
            spectrumData.setName(fileName);
            double[] idenRes = CommonScript.identification(spectrumData);

            List<String> dbNames = CommonScript.getAllFileNames("/Users/zmy/Project/spectrum_analysis/spectrum/src/main/resources/dbData_fix_param_peak_2");
            res.setIdenData(new ArrayList<>());
            for(int i = 0; i < idenRes.length; i++){
                if(idenRes[i] != 0){
                    String filePath1 = "dbData_fix_param_peak_2/" + dbNames.get(i);
                    ClassPathResource classPathResource1 = new ClassPathResource(filePath1);
                    InputStream inputStream1 = classPathResource1.getInputStream();

                    // 读取 JSON 文件内容
                    byte[] bytes1 = new byte[inputStream1.available()];
                    inputStream1.read(bytes1);
                    // 使用 FastJSON 解析 JSON 文件
                    String jsonContent1 = new String(bytes1, StandardCharsets.UTF_8);
                    JSONObject jsonObject1 = JSONObject.parseObject(jsonContent1);

                    List<Double> xData1 = jsonObject1.getJSONArray("ramanShift").toJavaList(Double.class);
                    List<Double> yData1 = jsonObject1.getJSONArray("curve").toJavaList(Double.class);
                    res.idenData.add(new SpectrumDataTest(xData1, yData1, dbNames.get(i), idenRes[i]));
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

    private List<String> getAllFileName(Integer type){
        List<String> res = new ArrayList<>();
        try {
            // 获取资源解析器
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // 使用相对路径获取resources目录下所有文件
            Resource[] resources = type == 1 ? resolver.getResources("classpath:sampleData/**") : resolver.getResources("classpath:mixedData/**");

            // 遍历输出文件名
            for (Resource resource : resources) {
                res.add(resource.getFilename());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static TestApiRes pretreatment(SpectrumData data, TestApiParams params){
        TestApiRes res = new TestApiRes();
        res.setXData(data.getRamanShift());
        res.setOriginY(data.getCurve());
        // x轴不变
        // 下面是对y轴的处理
        // 1、初步平滑
        List<Double> smooth = Whittaker.smooth(params.getLambdaA(), data.getCurve(), new ArrayList<>());
        res.setSm1(smooth);
        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), params.getAC(), 5, params.getAlpha());
        List<Double> background = Whittaker.smooth(params.getLambdaB(), data.getCurve(), peakIds);
        res.setBackground(background);
        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(data.getCurve(), background);
        res.setCorrected(corrected);
        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(params.getLambdaA(), corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        res.setSm2(sm2);
        return res;
    }

}
