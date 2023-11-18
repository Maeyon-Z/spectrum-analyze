package com.hit.spectrum.test;

import javafx.scene.control.DatePicker;
import lombok.Data;

@Data
public class TestApiParams {

    private Integer id;

    private Integer start;

    private Integer end;

    private Double lambdaA;


    private Double aC;

    private Double lambdaB;

    private Double alpha;

    public static TestApiParams build(Integer start, Integer end, Double lambdaA, Double lambdaB, Double ac, Double alpha){
        TestApiParams params = new TestApiParams();
        params.setAC(ac);
        params.setStart(start);
        params.setEnd(end);
        params.setLambdaA(lambdaA);
        params.setLambdaB(lambdaB);
        params.setAlpha(alpha);
        return params;
    }
}
