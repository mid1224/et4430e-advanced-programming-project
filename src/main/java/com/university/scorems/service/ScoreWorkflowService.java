package com.university.scorems.service;

import com.university.scorems.dto.ClassResponse;
import com.university.scorems.dto.CourseResponse;
import com.university.scorems.dto.EligibilityResponse;
import com.university.scorems.dto.ExamScoreRequest;
import com.university.scorems.dto.FinalResultResponse;
import com.university.scorems.dto.ResultFormResponse;
import com.university.scorems.dto.ScoreResponse;
import com.university.scorems.dto.ScoreUpsertRequest;
import com.university.scorems.exception.BadRequestException;
import com.university.scorems.exception.ConflictException;
import com.university.scorems.exception.ForbiddenException;
import com.university.scorems.exception.NotFoundException;
import com.university.scorems.model.Course;
import com.university.scorems.model.ResultForm;
import com.university.scorems.model.Score;
import com.university.scorems.model.Student;
import com.university.scorems.model.enums.GradeLetter;
import com.university.scorems.repository.CourseRepository;
import com.university.scorems.repository.ResultFormRepository;
import com.university.scorems.repository.ScoreRepository;
import com.university.scorems.repository.StudentRepository;
import com.university.scorems.repository.TeacherCourseClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreWorkflowService {

    private final TeacherCourseClassRepository teacherCourseClassRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ScoreRepository scoreRepository;
    private final ResultFormRepository resultFormRepository;

    @Transactional(readOnly = true)
    public List<ClassResponse> getClasses(Long teacherId) {
        return teacherCourseClassRepository.findDistinctCoursesByTeacherId(teacherId).stream()
            .map(c -> new ClassResponse(c.getId(), buildClassName(c)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses(Long teacherId) {
        return teacherCourseClassRepository.findDistinctCoursesByTeacherId(teacherId).stream()
            .map(c -> new CourseResponse(c.getId(), buildClassName(c), c.getCredits()))
                .toList();
    }

    @Transactional
    public ScoreResponse createScore(Long teacherId, ScoreUpsertRequest request) {
        validateRequiredIds(request.getStudentId(), request.getCourseId());
        validateScoreRange(request.getRegularScore(), "regularScore");
        validateScoreRange(request.getPeriodicScore(), "periodicScore");

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course not found"));

        ensureTeacherCanAccess(teacherId, student, course);
        if (scoreRepository.findByStudentIdAndCourseId(student.getId(), course.getId()).isPresent()) {
            throw new ConflictException("Score already exists for this student and course");
        }

        Score score = new Score();
        score.setStudent(student);
        score.setCourse(course);
        score.setRegularScore(request.getRegularScore());
        score.setPeriodicScore(request.getPeriodicScore());
        score.setAverageScore(calculateAverage(request.getRegularScore(), request.getPeriodicScore()));
        Score saved = scoreRepository.save(score);
        markStep1Forms(saved);
        return toScoreResponse(saved);
    }

    @Transactional
    public ScoreResponse updateScoreByStudent(Long teacherId, Long studentId, Long courseId, ScoreUpsertRequest request) {
        validateRequiredIds(studentId, courseId);
        validateScoreRange(request.getRegularScore(), "regularScore");
        validateScoreRange(request.getPeriodicScore(), "periodicScore");

        Score score = scoreRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new NotFoundException("Score not found"));
        ensureTeacherCanAccess(teacherId, score.getStudent(), score.getCourse());
        if (score.isFinalized()) {
            throw new ConflictException("Score finalized, cannot edit");
        }

        if (!score.getStudent().getId().equals(request.getStudentId())
                || !score.getCourse().getId().equals(request.getCourseId())
                || !request.getStudentId().equals(studentId)
                || !request.getCourseId().equals(courseId)) {
            throw new BadRequestException("studentId/courseId cannot be changed");
        }

        score.setRegularScore(request.getRegularScore());
        score.setPeriodicScore(request.getPeriodicScore());
        score.setAverageScore(calculateAverage(request.getRegularScore(), request.getPeriodicScore()));
        Score saved = scoreRepository.save(score);
        markStep1Forms(saved);
        return toScoreResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EligibilityResponse> getEligibility(Long teacherId, Long courseId) {
        ensureTeacherCanAccessCourse(teacherId, courseId);
        List<Score> scores = scoreRepository.findByCourseId(courseId).stream()
                .filter(s -> isTeacherAllowed(teacherId, s.getStudent().getStudentClass().getId(), s.getCourse().getId()))
                .toList();

        scores.forEach(this::markStep2Form);

        return scores.stream()
                .map(s -> EligibilityResponse.builder()
                        .studentId(s.getStudent().getId())
                        .studentName(s.getStudent().getName())
                        .courseId(s.getCourse().getId())
                        .courseName(s.getCourse().getName())
                        .averageScore(s.getAverageScore())
                        .eligible(s.getAverageScore().compareTo(BigDecimal.valueOf(4.0)) >= 0)
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScoreResponse> getScoresByCourse(Long teacherId, Long courseId) {
        ensureTeacherCanAccessCourse(teacherId, courseId);
        return scoreRepository.findByCourseId(courseId).stream()
                .filter(s -> isTeacherAllowed(teacherId, s.getStudent().getStudentClass().getId(), s.getCourse().getId()))
                .map(this::toScoreResponse)
                .toList();
    }

    @Transactional
    public FinalResultResponse inputExamScore(Long teacherId, ExamScoreRequest request) {
        validateRequiredIds(request.getStudentId(), request.getCourseId());
        validateScoreRange(request.getExamScore(), "examScore");
        Score score = scoreRepository.findByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())
                .orElseThrow(() -> new NotFoundException("Score not found"));
        ensureTeacherCanAccess(teacherId, score.getStudent(), score.getCourse());

        if (score.isFinalized()) {
            throw new ConflictException("Already finalized");
        }
        if (score.getAverageScore().compareTo(BigDecimal.valueOf(4.0)) < 0) {
            throw new BadRequestException("Student is not eligible");
        }

        score.setExamScore(request.getExamScore());
        score.setFinalScore(calculateFinal(score.getAverageScore(), request.getExamScore()));
        score.setFinalized(true);
        scoreRepository.save(score);

        ResultForm form = getOrCreateForm(score);
        form.setExamScoreRecorded(true);
        resultFormRepository.save(form);

        GradeLetter letter = toGradeLetter(score.getFinalScore());
        return FinalResultResponse.builder()
                .studentId(score.getStudent().getId())
                .studentName(score.getStudent().getName())
                .courseId(score.getCourse().getId())
                .courseName(score.getCourse().getName())
                .finalScore(score.getFinalScore())
                .gradeLetter(letter)
                .grade4Scale(toGrade4Scale(letter))
                .build();
    }

    @Transactional(readOnly = true)
    public List<FinalResultResponse> getFinalResults(Long teacherId, Long courseId) {
        ensureTeacherCanAccessCourse(teacherId, courseId);
        return scoreRepository.findByCourseId(courseId).stream()
                .filter(Score::isFinalized)
                .filter(s -> isTeacherAllowed(teacherId, s.getStudent().getStudentClass().getId(), s.getCourse().getId()))
                .map(s -> {
                    GradeLetter letter = toGradeLetter(s.getFinalScore());
                    return FinalResultResponse.builder()
                            .studentId(s.getStudent().getId())
                            .studentName(s.getStudent().getName())
                            .courseId(s.getCourse().getId())
                            .courseName(s.getCourse().getName())
                            .finalScore(s.getFinalScore())
                            .gradeLetter(letter)
                            .grade4Scale(toGrade4Scale(letter))
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResultFormResponse> getResultForms(Long teacherId, Long courseId) {
        ensureTeacherCanAccessCourse(teacherId, courseId);
        return resultFormRepository.findByCourseId(courseId).stream()
                .filter(f -> isTeacherAllowed(teacherId, f.getStudent().getStudentClass().getId(), f.getCourse().getId()))
                .map(this::toResultFormResponse)
                .toList();
    }

    @Transactional
    public ResultFormResponse submitResultFormByStudent(Long teacherId, Long studentId, Long courseId) {
        Score score = scoreRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new NotFoundException("Score not found"));
        ensureTeacherCanAccess(teacherId, score.getStudent(), score.getCourse());
        if (!score.isFinalized()) {
            throw new BadRequestException("Cannot submit form before finalizing score");
        }

        ResultForm form = resultFormRepository.findByScoreId(score.getId())
                .orElseThrow(() -> new NotFoundException("Result form not found"));
        form.setSubmittedToTrainingOffice(true);
        form.setSubmittedAt(LocalDateTime.now());
        return toResultFormResponse(resultFormRepository.save(form));
    }

    @Transactional
    public int submitResultFormByClass(Long teacherId, Long courseId, Long classId) {
        if (!courseId.equals(classId)) {
            throw new BadRequestException("classId must match courseId");
        }

        if (!teacherCourseClassRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
            throw new ForbiddenException("Teacher does not have access");
        }

        List<ResultForm> forms = resultFormRepository.findByCourseId(courseId);
        if (forms.isEmpty()) {
            throw new NotFoundException("No result forms found for this class");
        }

        if (forms.stream().anyMatch(f -> !f.getScore().isFinalized())) {
            throw new BadRequestException("Cannot submit BM03 before all class scores are finalized");
        }

        LocalDateTime now = LocalDateTime.now();
        forms.forEach(form -> {
            form.setSubmittedToTrainingOffice(true);
            form.setSubmittedAt(now);
        });
        resultFormRepository.saveAll(forms);
        return forms.size();
    }

    private ScoreResponse toScoreResponse(Score score) {
        return ScoreResponse.builder()
                .studentId(score.getStudent().getId())
                .studentName(score.getStudent().getName())
                .classId(score.getCourse().getId())
                .className(buildClassName(score.getCourse()))
                .courseId(score.getCourse().getId())
                .courseName(score.getCourse().getName())
                .regularScore(score.getRegularScore())
                .periodicScore(score.getPeriodicScore())
                .examScore(score.getExamScore())
                .averageScore(score.getAverageScore())
                .finalScore(score.getFinalScore())
                .finalized(score.isFinalized())
                .build();
    }

    private ResultFormResponse toResultFormResponse(ResultForm form) {
        Score score = form.getScore();
        return ResultFormResponse.builder()
                .formId(form.getId())
                .formCode(form.getFormCode())
                .studentId(form.getStudent().getId())
                .classId(form.getCourse().getId())
                .studentName(form.getStudent().getName())
                .className(buildClassName(form.getCourse()))
                .courseId(form.getCourse().getId())
                .courseName(form.getCourse().getName())
                .regularScore(score.getRegularScore())
                .periodicScore(score.getPeriodicScore())
                .averageScore(score.getAverageScore())
                .examScore(score.getExamScore())
                .finalScore(score.getFinalScore())
                .eligibilityReviewed(form.isEligibilityReviewed())
                .examScoreRecorded(form.isExamScoreRecorded())
                .submittedToTrainingOffice(form.isSubmittedToTrainingOffice())
                .submittedAt(form.getSubmittedAt())
                .generatedAt(form.getCreatedAt())
                .build();
    }

    private void markStep1Forms(Score score) {
        ResultForm form = getOrCreateForm(score);
        resultFormRepository.save(form);
    }

    private void markStep2Form(Score score) {
        ResultForm form = getOrCreateForm(score);
        form.setEligibilityReviewed(true);
        resultFormRepository.save(form);
    }

    private ResultForm getOrCreateForm(Score score) {
        ResultForm form = resultFormRepository.findByStudentIdAndCourseId(score.getStudent().getId(), score.getCourse().getId())
                .orElseGet(ResultForm::new);
        form.setStudent(score.getStudent());
        form.setCourse(score.getCourse());
        form.setScore(score);
        form.setFormCode("BM/QLD/DTNCKH/03");
        return form;
    }

    private BigDecimal calculateAverage(BigDecimal regular, BigDecimal periodic) {
        return regular
                .add(periodic.multiply(BigDecimal.valueOf(2)))
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinal(BigDecimal average, BigDecimal exam) {
        return average.multiply(BigDecimal.valueOf(0.4))
                .add(exam.multiply(BigDecimal.valueOf(0.6)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private GradeLetter toGradeLetter(BigDecimal finalScore) {
        if (finalScore.compareTo(BigDecimal.valueOf(8.5)) >= 0) return GradeLetter.A;
        if (finalScore.compareTo(BigDecimal.valueOf(7.0)) >= 0) return GradeLetter.B;
        if (finalScore.compareTo(BigDecimal.valueOf(5.5)) >= 0) return GradeLetter.C;
        if (finalScore.compareTo(BigDecimal.valueOf(4.0)) >= 0) return GradeLetter.D;
        return GradeLetter.F;
    }

    private BigDecimal toGrade4Scale(GradeLetter letter) {
        return switch (letter) {
            case A -> BigDecimal.valueOf(4.0);
            case B -> BigDecimal.valueOf(3.0);
            case C -> BigDecimal.valueOf(2.0);
            case D -> BigDecimal.valueOf(1.0);
            case F -> BigDecimal.valueOf(0.0);
        };
    }

    private void validateScoreRange(BigDecimal score, String name) {
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new BadRequestException(name + " must be between 0 and 10");
        }
    }

    private void validateRequiredIds(Long studentId, Long courseId) {
        if (studentId == null) {
            throw new BadRequestException("studentId is required");
        }
        if (courseId == null) {
            throw new BadRequestException("courseId is required");
        }
    }

    private void ensureTeacherCanAccess(Long teacherId, Student student, Course course) {
        if (!isTeacherAllowed(teacherId, student.getStudentClass().getId(), course.getId())) {
            throw new ForbiddenException("Teacher does not have access");
        }
    }

    private void ensureTeacherCanAccessCourse(Long teacherId, Long courseId) {
        if (!teacherCourseClassRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
            throw new ForbiddenException("Teacher does not have access");
        }
    }

    private boolean isTeacherAllowed(Long teacherId, Long classId, Long courseId) {
        return teacherCourseClassRepository.existsByTeacherIdAndCourseIdAndStudentClassId(teacherId, courseId, classId);
    }

    private String buildClassName(Course course) {
        return course.getName() + " - HK" + course.getSemester() + " - L" + course.getClassIndex();
    }
}
