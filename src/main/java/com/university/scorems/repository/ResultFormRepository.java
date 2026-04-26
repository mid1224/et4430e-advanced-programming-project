package com.university.scorems.repository;

import com.university.scorems.model.ResultForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultFormRepository extends JpaRepository<ResultForm, Long> {
    Optional<ResultForm> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<ResultForm> findByScoreId(Long scoreId);

    List<ResultForm> findByCourseId(Long courseId);

    List<ResultForm> findByCourseIdAndStudentStudentClassId(Long courseId, Long classId);
}
