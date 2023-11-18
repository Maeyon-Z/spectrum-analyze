package com.hit.spectrum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
