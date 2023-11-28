package com.hit.spectrum.test;

import com.hit.spectrum.data.SpectrumData;
import lombok.Data;

import java.util.List;

@Data
public class TestApiRes {

    private List<Double> xData;

    private List<Double> originY;

    private List<Double> sm1;

    private List<Double> background;

    private List<Double> corrected;

    private List<Double> sm2;

    private String other;

    private List<Double> yData;

    private List<Double> dbData;

    List<SpectrumDataTest> idenData;
}
