package com.hit.spectrum.test;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DbData {
    private String name;

    private List<Double> data;
}
