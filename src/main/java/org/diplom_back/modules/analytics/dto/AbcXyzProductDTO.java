package org.diplom_back.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AbcXyzProductDTO {
    private String name;
    private String abcGroup;  // "A", "B", "C"
    private String xyzGroup;  // "X", "Y", "Z"
    private double totalRevenue;
    private double variationCoefficient; // CV в %
}