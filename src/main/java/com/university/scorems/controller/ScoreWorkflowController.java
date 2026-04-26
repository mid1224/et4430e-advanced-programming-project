package com.university.scorems.controller;

import com.university.scorems.dto.EligibilityResponse;
import com.university.scorems.dto.ExamScoreRequest;
import com.university.scorems.dto.FinalResultResponse;
import com.university.scorems.dto.ResultFormResponse;
import com.university.scorems.dto.ScoreResponse;
import com.university.scorems.dto.ScoreUpsertRequest;
import com.university.scorems.dto.ApiResponse;
import com.university.scorems.service.ScoreWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScoreWorkflowController {

    private final ScoreWorkflowService scoreWorkflowService;

    @PostMapping("/scores")
    public ResponseEntity<ScoreResponse> createScore(@RequestParam Long teacherId,
                                                     @RequestBody ScoreUpsertRequest request) {
        return ResponseEntity.ok(scoreWorkflowService.createScore(teacherId, request));
    }

    @GetMapping("/scores/course/{courseId}")
    public ResponseEntity<List<ScoreResponse>> getScoresByCourse(@PathVariable Long courseId,
                                                                 @RequestParam Long teacherId) {
        return ResponseEntity.ok(scoreWorkflowService.getScoresByCourse(teacherId, courseId));
    }

    @PutMapping("/scores/student/{studentId}")
    public ResponseEntity<ScoreResponse> updateScore(@PathVariable Long studentId,
                                                     @RequestParam Long teacherId,
                                                     @RequestParam Long courseId,
                                                     @RequestBody ScoreUpsertRequest request) {
        return ResponseEntity.ok(scoreWorkflowService.updateScoreByStudent(teacherId, studentId, courseId, request));
    }

    @GetMapping("/eligibility/{courseId}")
    public ResponseEntity<List<EligibilityResponse>> getEligibility(@PathVariable Long courseId,
                                                                    @RequestParam Long teacherId) {
        return ResponseEntity.ok(scoreWorkflowService.getEligibility(teacherId, courseId));
    }

    @PostMapping("/exam-score")
    public ResponseEntity<FinalResultResponse> inputExamScore(@RequestParam Long teacherId,
                                                              @RequestBody ExamScoreRequest request) {
        return ResponseEntity.ok(scoreWorkflowService.inputExamScore(teacherId, request));
    }

    @GetMapping("/final-result/{courseId}")
    public ResponseEntity<List<FinalResultResponse>> getFinalResult(@PathVariable Long courseId,
                                                                     @RequestParam Long teacherId) {
        return ResponseEntity.ok(scoreWorkflowService.getFinalResults(teacherId, courseId));
    }

    @GetMapping("/result-form/{courseId}")
    public ResponseEntity<List<ResultFormResponse>> getResultForms(@PathVariable Long courseId,
                                                                   @RequestParam Long teacherId) {
        return ResponseEntity.ok(scoreWorkflowService.getResultForms(teacherId, courseId));
    }

    @PostMapping("/result-form/submit/{studentId}")
    public ResponseEntity<ResultFormResponse> submitResultForm(@PathVariable Long studentId,
                                                               @RequestParam Long teacherId,
                                                               @RequestParam Long courseId) {
        return ResponseEntity.ok(scoreWorkflowService.submitResultFormByStudent(teacherId, studentId, courseId));
    }

    @PostMapping("/result-form/submit/class/{classId}")
    public ResponseEntity<ApiResponse> submitResultFormByClass(@PathVariable Long classId,
                                                               @RequestParam Long teacherId,
                                                               @RequestParam Long courseId) {
        int submittedCount = scoreWorkflowService.submitResultFormByClass(teacherId, courseId, classId);
        return ResponseEntity.ok(new ApiResponse("Submitted BM03 for class " + classId + " (" + submittedCount + " records)"));
    }
}
