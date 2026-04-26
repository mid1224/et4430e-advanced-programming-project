USE student_score_management;

INSERT INTO student_classes (id, name)
VALUES (1, 'SE2024A'),
       (2, 'SE2024B')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO students (id, name, class_id)
VALUES (1, 'Nguyen Van A', 1),
       (2, 'Tran Thi B', 1),
       (3, 'Le Van C', 2)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO courses (id, name, credits, semester, class_index)
VALUES (1, 'Advanced Programming', 3, 1, 1),
       (2, 'Database Systems', 3, 1, 2)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    credits = VALUES(credits),
    semester = VALUES(semester),
    class_index = VALUES(class_index);

INSERT INTO teacher_course_classes (teacher_id, course_id, class_id)
VALUES (1, 1, 1),
       (1, 2, 2);
