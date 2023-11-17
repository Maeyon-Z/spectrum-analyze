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

//@RestController
@RequestMapping("/spectrum")
public class TestApi {

    private SpectrumService service = new SpectrumService();

    @GetMapping("/getAllData")
    public List<SampleName> getAllData(){
        List<SampleName> res = new ArrayList<>();
        List<String> fileNames = getAllSampleData();
        for (int i = 0; i < fileNames.size(); i++){
            res.add(new SampleName(i, fileNames.get(i)));
        }
        return res;
    }

    //标准品入库测试接口
    @GetMapping("/test/{id}")
    public TestApiRes test(@PathVariable("id") Integer id){
        List<String> fileNames = getAllSampleData();
        if(id >= fileNames.size()){
            return null;
        }
        String fileName = fileNames.get(id);
        TestApiRes res = new TestApiRes();
        try {
            String filePath = "sampleData/" + fileName;
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
            xData = xData.subList(300, 1000);
            yData = yData.subList(300, 1000);
            SpectrumData spectrumData = new SpectrumData();
            spectrumData.setCurve(yData);
            spectrumData.setRamanShift(xData);
            res = pretreatment(spectrumData);
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

    private List<String> getAllSampleData(){
        List<String> res = new ArrayList<>();
        try {
            // 获取资源解析器
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // 使用相对路径获取resources目录下所有文件
            Resource[] resources = resolver.getResources("classpath:sampleData/**");

            // 遍历输出文件名
            for (Resource resource : resources) {
                res.add(resource.getFilename());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static TestApiRes pretreatment(SpectrumData data){
        TestApiRes res = new TestApiRes();
        res.setXData(data.getRamanShift());
        res.setOriginY(data.getCurve());
        // x轴不变
        // 下面是对y轴的处理
        // 1、初步平滑
        List<Double> smooth = Whittaker.smooth(2.0, data.getCurve(), new ArrayList<>());
        res.setSm1(smooth);
        // 2、提取背景荧光
        List<Integer> peakIds = PeakSearch.search(DataConvertUtils.list2Array(smooth), -0.1, 5, 0.2);
        List<Double> background = Whittaker.smooth(1000.0, data.getCurve(), peakIds);
        res.setBackground(background);
        // 3、消除荧光，校准基线
        List<Double> corrected = Calibration.correct(data.getCurve(), background);
        res.setCorrected(corrected);
        // 4、第二步平滑
        List<Double> sm2 = Whittaker.smooth(2.0, corrected, new ArrayList<>());
        // 处理负值
        double min = Collections.min(sm2);
        if(min < 0) {
            sm2.replaceAll(aDouble -> aDouble - min);
        }
        res.setSm2(sm2);
        return res;
    }

}
