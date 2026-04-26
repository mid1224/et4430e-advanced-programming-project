package com.university.scorems.dto;

import com.university.scorems.model.enums.GradeLetter;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinalResultResponse {
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private BigDecimal finalScore;
    private GradeLetter gradeLetter;
    private BigDecimal grade4Scale;
}
