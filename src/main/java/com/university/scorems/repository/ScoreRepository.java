package com.university.scorems.repository;

import com.university.scorems.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Score> findByCourseId(Long courseId);
}
