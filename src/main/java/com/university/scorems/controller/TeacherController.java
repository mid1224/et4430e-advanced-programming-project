package com.university.scorems.controller;

import com.university.scorems.dto.ClassResponse;
import com.university.scorems.dto.CourseResponse;
import com.university.scorems.service.ScoreWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeacherController {

    private final ScoreWorkflowService scoreWorkflowService;

    @GetMapping("/classes/{teacherId}")
    public ResponseEntity<List<ClassResponse>> getClasses(@PathVariable Long teacherId) {
        return ResponseEntity.ok(scoreWorkflowService.getClasses(teacherId));
    }

    @GetMapping("/courses/{teacherId}")
    public ResponseEntity<List<CourseResponse>> getCourses(@PathVariable Long teacherId) {
        return ResponseEntity.ok(scoreWorkflowService.getCourses(teacherId));
    }
}
