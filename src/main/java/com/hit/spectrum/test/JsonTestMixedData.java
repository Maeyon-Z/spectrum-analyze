package com.hit.spectrum.test;

import lombok.Data;

import java.util.List;

@Data
public class JsonTestMixedData {

    private List<Double> raman_shift;

    private List<Double> curve;

    private List<String> names;
}
