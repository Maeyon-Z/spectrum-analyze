package com.hit.spectrum.data;

import lombok.Data;

@Data
public class SpectrumDbData {

    private Long id;

    private String name;

    private String origin;

    private String smoothOne;

    private String background;

    private String corrected;

    private String smoothTwo;

    private String fixPeak;

    private String normalized;
}
