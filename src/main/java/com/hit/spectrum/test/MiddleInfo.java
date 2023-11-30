package com.hit.spectrum.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MiddleInfo {

    private List<Double> x0;

    private List<Double> x1;

    private List<Double> x2;

}
