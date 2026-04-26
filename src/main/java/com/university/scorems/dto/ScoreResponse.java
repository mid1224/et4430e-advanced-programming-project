package com.university.scorems.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ScoreResponse {
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private Long courseId;
    private String courseName;
    private BigDecimal regularScore;
    private BigDecimal periodicScore;
    private BigDecimal examScore;
    private BigDecimal averageScore;
    private BigDecimal finalScore;
    private boolean finalized;
}
