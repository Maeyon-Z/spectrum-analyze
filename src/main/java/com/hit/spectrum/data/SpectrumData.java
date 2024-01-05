package com.hit.spectrum.data;

import lombok.Data;

import java.util.List;

@Data
public class SpectrumData {
    private Long id;

    private String name;

    private List<Double> origin;

    private List<Double> smoothOne;

    private List<Double> background;

    private List<Double> corrected;

    private List<Double> smoothTwo;

    private List<Double> fixPeak;

    private List<Double> normalized;
}
