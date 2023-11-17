package com.hit.spectrum.data;

import lombok.Data;

import java.util.List;

@Data
public class SpectrumData {

    /**
     * 横坐标
     */
    private List<Double> ramanShift;

    /**
     * 纵坐标
     */
    private List<Double> curve;

    /**
     * 样品名称
     */
    private String name;

}
