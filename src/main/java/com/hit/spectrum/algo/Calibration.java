package com.hit.spectrum.algo;

import java.util.ArrayList;
import java.util.List;

public class Calibration {

    public static List<Double> correct(List<Double> originData, List<Double> background){
        List<Double> corrected = new ArrayList<>();
        for(int i = 0; i < background.size(); i++) corrected.add(originData.get(i) - background.get(i));
        return corrected;
    }
}
