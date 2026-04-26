package com.university.scorems.service;

import com.university.scorems.exception.BadRequestException;
import com.university.scorems.exception.ForbiddenException;
import com.university.scorems.model.Course;
import com.university.scorems.model.Score;
import com.university.scorems.model.enums.GradeLetter;
import com.university.scorems.repository.ScoreRepository;
import com.university.scorems.repository.TeacherCourseClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormRenderingService {

    private final ScoreRepository scoreRepository;
    private final TeacherCourseClassRepository teacherCourseClassRepository;

    @Transactional(readOnly = true)
    public String renderBm03(Long teacherId, Long courseId, Long classId) {
        List<Score> scores = loadScoresForTeacher(teacherId, courseId, classId);
        String courseName = scores.isEmpty() ? "" : scores.get(0).getCourse().getName();
        String classNames = scores.isEmpty() ? "" : buildClassName(scores.get(0).getCourse());

        StringBuilder rows = new StringBuilder();
        int stt = 1;
        int countA = 0;
        int countB = 0;
        int countC = 0;
        int countD = 0;
        int countF = 0;

        for (Score score : scores) {
            GradeLetter letter = score.getFinalScore() == null ? null : toGradeLetter(score.getFinalScore());
            if (letter != null) {
                switch (letter) {
                    case A -> countA++;
                    case B -> countB++;
                    case C -> countC++;
                    case D -> countD++;
                    case F -> countF++;
                }
            }
            rows.append("<tr>")
                    .append("<td class='center'>").append(stt++).append("</td>")
                    .append("<td class='center'>").append(score.getStudent().getId()).append("</td>")
                    .append("<td>").append(escape(score.getStudent().getName())).append("</td>")
                    .append("<td class='center'>").append(formatNumber(score.getRegularScore())).append("</td>")
                    .append("<td class='center'>").append(formatNumber(score.getPeriodicScore())).append("</td>")
                    .append("<td class='center'>").append(formatNumber(score.getAverageScore())).append("</td>")
                    .append("<td class='center'>").append(formatNumber(score.getExamScore())).append("</td>")
                    .append("<td class='center'>").append(formatNumber(score.getFinalScore())).append("</td>")
                    .append("<td class='center'>").append(letter == null ? "" : letter.name()).append("</td>")
                    .append("<td class='center'>").append(letter == null ? "" : formatNumber(toGrade4Scale(letter))).append("</td>")
                    .append("<td></td>")
                    .append("<td></td>")
                    .append("</tr>");
        }

        int minRows = 8;
        for (int i = scores.size() + 1; i <= minRows; i++) {
            rows.append("<tr>")
                    .append("<td class='center'>").append(i).append("</td>")
                    .append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>")
                    .append("</tr>");
        }

        int totalWithFinal = countA + countB + countC + countD + countF;

        return """
                <!DOCTYPE html>
                <html lang='vi'>
                <head>
                    <meta charset='UTF-8'>
                    <title>BM/QLD/DTNCKH/03</title>
                    <style>
                        @page { size: A4 portrait; margin: 16mm; }
                        body { font-family: 'Times New Roman', serif; color: #111; }
                        .sheet { max-width: 190mm; margin: 0 auto; }
                        .code { text-align: right; color: #b30000; font-size: 18px; margin-bottom: 6px; }
                        .org { display: flex; justify-content: space-between; margin-top: 8px; }
                        .org-left, .org-right { font-weight: 700; font-size: 14px; line-height: 1.35; }
                        .org-right { text-align: center; }
                        .main-title { text-align: center; font-size: 24px; font-weight: 700; margin: 10px 0 12px; }
                        .meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 18px; font-size: 13px; margin-bottom: 8px; }
                        .meta-item { min-height: 20px; }
                        table { width: 100%%; border-collapse: collapse; table-layout: fixed; margin-top: 6px; }
                        th, td { border: 1px solid #222; padding: 4px 3px; font-size: 11px; }
                        th { text-align: center; vertical-align: middle; }
                        .small { font-style: italic; font-weight: 400; }
                        .center { text-align: center; }
                        .ranking { margin-top: 8px; font-size: 11px; }
                        .ranking-title { font-weight: 700; margin-bottom: 6px; }
                        .ranking-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px 18px; }
                        .signatures { display: flex; justify-content: space-between; align-items: flex-start; margin-top: 16px; }
                        .signatures div { width: 32%%; text-align: center; font-size: 11px; display: flex; flex-direction: column; align-items: center; }
                        .signatures .role { font-weight: 700; font-size: 16px; margin-bottom: 6px; min-height: 58px; line-height: 1.1; display: flex; align-items: flex-start; justify-content: center; }
                        .date { text-align: right; margin-top: 8px; font-size: 11px; }
                    </style>
                </head>
                <body>
                <div class='sheet'>
                    <div class='code'>BM/QLD/DTNCKH/03</div>
                    <div class='org'>
                        <div class='org-left'>
                            BO CONG THUONG<br>
                            TRUONG CAO DANG<br>
                            CONG NGHIEP THAI NGUYEN<br>
                            KHOA: ___________________<br>
                            LOP, KHOA: %s
                        </div>
                        <div class='org-right'>
                            CONG HOA XA HOI CHU NGHIA VIET NAM<br>
                            Doc lap - Tu do - Hanh phuc
                        </div>
                    </div>

                    <div class='main-title'>PHIEU GHI KET QUA HOC TAP</div>

                    <div class='meta-grid'>
                        <div class='meta-item'><strong>Ten MH/MD:</strong> %s</div>
                        <div class='meta-item'><strong>Hoc ky:</strong> __________</div>
                        <div class='meta-item'><strong>Ma MH:</strong> __________</div>
                        <div class='meta-item'><strong>Nam hoc:</strong> __________</div>
                        <div class='meta-item'><strong>Hinh thuc thi ket thuc:</strong> __________</div>
                        <div class='meta-item'><strong>Lan thi thu:</strong> __________</div>
                    </div>

                    <table>
                        <thead>
                        <tr>
                            <th rowspan='2' style='width: 5%%'>TT</th>
                            <th rowspan='2' style='width: 8%%'>MSSV</th>
                            <th rowspan='2' style='width: 18%%'>Ho va ten</th>
                            <th>Diem KT thuong xuyen MH/MD</th>
                            <th>Diem KT dinh ky MH/MD</th>
                            <th>Diem TBC kiem tra</th>
                            <th>Diem kiem tra ket thuc MH/MD</th>
                            <th>Diem TK</th>
                            <th rowspan='2' style='width: 7%%'>Diem chu</th>
                            <th>Quy doi sang diem so</th>
                            <th rowspan='2' style='width: 7%%'>Chu ky SV</th>
                            <th rowspan='2' style='width: 7%%'>Ghi chu</th>
                        </tr>
                        <tr>
                            <th class='small'>He so 1</th>
                            <th class='small'>He so 2</th>
                            <th class='small'>TB he so 1:2</th>
                            <th class='small'>He so 0,6 / Thang 10</th>
                            <th class='small'>Thang diem 10</th>
                            <th class='small'>Thang diem 4</th>
                        </tr>
                        </thead>
                        <tbody>
                        %s
                        </tbody>
                    </table>

                    <div class='ranking'>
                        <div class='ranking-title'>%% Xep hang theo diem chu</div>
                        <div class='ranking-grid'>
                            <div>Loai A: HS = %s%%</div>
                            <div>Loai B: HS = %s%%</div>
                            <div>Loai C: HS = %s%%</div>
                            <div>Loai D: HS = %s%%</div>
                            <div>Loai F: HS = %s%%</div>
                        </div>
                    </div>

                    <div class='date'>Ngay ____ thang ____ nam ____</div>
                    <div class='signatures'>
                        <div>
                            <div class='role'>Noi gui</div>
                        </div>
                        <div>
                            <div class='role'>TRUONG KHOA</div>
                            (Ky, ghi ro ho ten)
                        </div>
                        <div>
                            <div class='role'>GIAO VIEN GIANG</div>
                            (Ky, ghi ro ho ten)
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(
                escape(classNames),
                escape(courseName),
                rows.toString(),
                formatPercent(countA, totalWithFinal),
                formatPercent(countB, totalWithFinal),
                formatPercent(countC, totalWithFinal),
                formatPercent(countD, totalWithFinal),
                formatPercent(countF, totalWithFinal)
        );
    }

    private List<Score> loadScoresForTeacher(Long teacherId, Long courseId, Long classId) {
        if (!teacherCourseClassRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
            throw new ForbiddenException("Teacher does not have access");
        }

        if (classId != null && !classId.equals(courseId)) {
            throw new BadRequestException("classId must match courseId");
        }

        return scoreRepository.findByCourseId(courseId).stream()
                .filter(s -> teacherCourseClassRepository.existsByTeacherIdAndCourseIdAndStudentClassId(
                        teacherId,
                        courseId,
                        s.getStudent().getStudentClass().getId()
                ))
                .sorted(Comparator.comparing((Score s) -> s.getStudent().getStudentClass().getName())
                        .thenComparing(s -> s.getStudent().getName()))
                .toList();
    }

            private String buildClassName(Course course) {
            return course.getName() + " - HK" + course.getSemester() + " - L" + course.getClassIndex();
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatPercent(int value, int total) {
        if (total == 0) {
            return "0.00";
        }
        BigDecimal percent = BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return percent.toPlainString();
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

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
