package com.university.scorems.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ResultFormResponse {
    private Long formId;
    private String formCode;
    private Long studentId;
    private Long classId;
    private String studentName;
    private String className;
    private Long courseId;
    private String courseName;
    private BigDecimal regularScore;
    private BigDecimal periodicScore;
    private BigDecimal averageScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private boolean eligibilityReviewed;
    private boolean examScoreRecorded;
    private boolean submittedToTrainingOffice;
    private LocalDateTime submittedAt;
    private LocalDateTime generatedAt;
}
