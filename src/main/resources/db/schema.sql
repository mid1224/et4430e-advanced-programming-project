CREATE DATABASE IF NOT EXISTS student_score_management;
USE student_score_management;

CREATE TABLE IF NOT EXISTS student_classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    class_id BIGINT NOT NULL,
    CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES student_classes(id)
);

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    credits INT NOT NULL,
    semester INT NOT NULL DEFAULT 1,
    class_index INT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS teacher_course_classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    CONSTRAINT uq_teacher_course_class UNIQUE (teacher_id, course_id, class_id),
    CONSTRAINT fk_tcc_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_tcc_class FOREIGN KEY (class_id) REFERENCES student_classes(id)
);

CREATE TABLE IF NOT EXISTS scores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    regular_score DECIMAL(4,2) NOT NULL,
    periodic_score DECIMAL(4,2) NOT NULL,
    exam_score DECIMAL(4,2) NULL,
    average_score DECIMAL(4,2) NOT NULL,
    final_score DECIMAL(4,2) NULL,
    finalized BIT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uq_student_course_score UNIQUE (student_id, course_id),
    CONSTRAINT fk_scores_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_scores_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS result_forms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    score_id BIGINT NOT NULL,
    form_code VARCHAR(40) NOT NULL DEFAULT 'BM/QLD/DTNCKH/03',
    eligibility_reviewed BIT(1) NOT NULL DEFAULT 0,
    exam_score_recorded BIT(1) NOT NULL DEFAULT 0,
    submitted_to_training_office BIT(1) NOT NULL DEFAULT 0,
    submitted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_result_form_student_course UNIQUE (student_id, course_id),
    CONSTRAINT uq_result_form_score UNIQUE (score_id),
    CONSTRAINT fk_result_form_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_result_form_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_result_form_score FOREIGN KEY (score_id) REFERENCES scores(id)
);

CREATE INDEX idx_scores_course ON scores(course_id);
CREATE INDEX idx_result_forms_course ON result_forms(course_id);
CREATE INDEX idx_tcc_teacher_course ON teacher_course_classes(teacher_id, course_id);
