package com.hit.spectrum.data;

import org.springframework.util.ResourceUtils;

import java.io.File;

// todo 改为从数据库加载数据
public class SpectrumDataLoader {

    public SpectrumData loadOriginData(String filePath, Integer begin, Integer end) throws Exception{
        SpectrumData data = new SpectrumData();
        File file = ResourceUtils.getFile("classpath:file.txt");
        return new SpectrumData();
    }
}
