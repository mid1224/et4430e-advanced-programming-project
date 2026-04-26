package com.university.scorems.repository;

import com.university.scorems.model.Course;
import com.university.scorems.model.StudentClass;
import com.university.scorems.model.TeacherCourseClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeacherCourseClassRepository extends JpaRepository<TeacherCourseClass, Long> {

    @Query("select distinct tcc.studentClass from TeacherCourseClass tcc where tcc.teacherId = :teacherId")
    List<StudentClass> findDistinctClassesByTeacherId(Long teacherId);

    @Query("select distinct tcc.course from TeacherCourseClass tcc where tcc.teacherId = :teacherId")
    List<Course> findDistinctCoursesByTeacherId(Long teacherId);

    boolean existsByTeacherIdAndCourseId(Long teacherId, Long courseId);

    boolean existsByTeacherIdAndCourseIdAndStudentClassId(Long teacherId, Long courseId, Long studentClassId);
}
