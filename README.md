# Student Score Management (Procedure-based Scope)

This project is scoped only by `quy trinh quan ly diem hoc tap.doc`.

## Scope
Only steps **1, 2, 6** in the **Dien giai** table are implemented.

Class naming rule:
- Class name is derived from course metadata:
- `ClassName = CourseName + " - HK" + semester + " - L" + classIndex`

1. Step 1 - Nhap diem thuong xuyen, dinh ky  
2. Step 2 - Xet dieu kien thi  
3. Step 6 - Nhap diem mon hoc/module va gui Phieu ket qua hoc tap ban goc ve Phong DT&NCKH

## Forms in Scope
- Step 1: `So tay giao vien`, `So len lop`
- Step 2: `Phieu ket qua hoc tap`
- Step 6: `Phieu ket qua hoc tap` (ban goc), `So tay giao vien`, `So len lop`

`result_forms` stores form-tracking fields:
- `teacherNotebookUpdated`
- `classNotebookUpdated`
- `eligibilityReviewed`
- `examScoreRecorded`
- `submittedToTrainingOffice`

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Data JPA
- MySQL

## APIs
Teacher scope:
- `GET /classes/{teacherId}`
- `GET /courses/{teacherId}`

Step 1:
- `POST /scores?teacherId={teacherId}`
- `PUT /scores/student/{studentId}?teacherId={teacherId}&courseId={courseId}`
- `GET /scores/course/{courseId}?teacherId={teacherId}`

Step 2:
- `GET /eligibility/{courseId}?teacherId={teacherId}`

Step 6:
- `POST /exam-score?teacherId={teacherId}`
- `GET /final-result/{courseId}?teacherId={teacherId}`
- `GET /result-form/{courseId}?teacherId={teacherId}`
- `POST /result-form/submit/{studentId}?teacherId={teacherId}&courseId={courseId}`
- `POST /result-form/submit/class/{classId}?teacherId={teacherId}&courseId={courseId}`

Generated forms:
- `GET /forms/bm03/{courseId}?teacherId={teacherId}&classId={classId}`

## Run
1. Run:
   - [schema.sql](/C:/MyFiles/GithubRepos/et4430e-advanced-programming-project/src/main/resources/db/schema.sql)
   - [sample-data.sql](/C:/MyFiles/GithubRepos/et4430e-advanced-programming-project/src/main/resources/db/sample-data.sql)
2. Configure DB in [application.yml](/C:/MyFiles/GithubRepos/et4430e-advanced-programming-project/src/main/resources/application.yml)
3. Start:
   - `mvn spring-boot:run`
4. Open frontend:
   - `http://localhost:8080`
