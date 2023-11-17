package com.hit.spectrum.test;

import com.alibaba.fastjson.JSON;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//@RestController
@RequestMapping("/spectrum")
public class TestApiServer {

    @GetMapping("/getAllData")
    public List<SampleName> getAllData(){
        List<SampleName> res = new ArrayList<>();
        List<String> fileNames = getAllFileNames("/root/spectrum/sampleData");
        for (int i = 0; i < fileNames.size(); i++){
            res.add(new SampleName(i, fileNames.get(i)));
        }
        return res;
    }

    //标准品入库测试接口
    @PostMapping("/test")
    public TestApiRes test(@RequestBody TestApiParams params){
        List<String> fileNames = getAllFileNames("/root/spectrum/sampleData");
        if(params.getId() >= fileNames.size()){
            return null;
        }
        String fileName = fileNames.get(params.getId());
        TestApiRes res = new TestApiRes();

        try {
            String filePath = "/root/spectrum/sampleData/" + fileName;
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
            xData = xData.subList(params.getStart(), params.getEnd());
            yData = yData.subList(params.getStart(), params.getEnd());
            SpectrumData spectrumData = new SpectrumData();
            spectrumData.setCurve(yData);
            spectrumData.setRamanShift(xData);
            res = pretreatment(spectrumData, params);
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

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
