package com.university.scorems.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "scores", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal regularScore;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal periodicScore;

    @Column(precision = 4, scale = 2)
    private BigDecimal examScore;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal averageScore;

    @Column(precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Column(nullable = false)
    private boolean finalized = false;
}
