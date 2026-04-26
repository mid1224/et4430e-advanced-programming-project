package com.university.scorems.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EligibilityResponse {
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private BigDecimal averageScore;
    private Boolean eligible;
}
