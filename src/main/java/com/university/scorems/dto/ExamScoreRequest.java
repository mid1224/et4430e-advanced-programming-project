package com.university.scorems.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExamScoreRequest {
    private Long studentId;
    private Long courseId;
    private BigDecimal examScore;
}
