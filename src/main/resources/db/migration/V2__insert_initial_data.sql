-- Insert classrooms
INSERT INTO classrooms (name, capacity)
VALUES ('Room 101', 2),
       ('Room 102', 3),
       ('Computer Lab 1', 2),
       ('Science Lab 1', 25);

-- Insert subjects with levels
INSERT INTO subjects (name, total_hours_per_week, level)
VALUES 
    ('Mathematics', 6, 1),
    ('Mathematics', 6, 2),
    ('Mathematics', 6, 3),
    ('Physics', 4, 1),
    ('Physics', 4, 2),
    ('Physics', 4, 3),
    ('Chemistry', 4, 1),
    ('Chemistry', 4, 2),
    ('Chemistry', 4, 3),
    ('Biology', 4, 1),
    ('Biology', 4, 2),
    ('Biology', 4, 3),
    ('Computer Science', 5, 1),
    ('Computer Science', 5, 2),
    ('Computer Science', 5, 3),
    ('English', 5, 1),
    ('English', 5, 2),
    ('English', 5, 3),
    ('History', 3, 1),
    ('History', 3, 2),
    ('History', 3, 3),
    ('Geography', 3, 1),
    ('Geography', 3, 2),
    ('Geography', 3, 3),
    ('Art', 3, 1),
    ('Art', 3, 2),
    ('Art', 3, 3);

-- Insert teachers
INSERT INTO teachers (name, surname, time_start, time_end)
VALUES ('John', 'Smith', '08:00:00', '16:00:00'),
       ('Mary', 'Johnson', '09:00:00', '17:00:00'),
       ('Robert', 'Williams', '08:30:00', '16:30:00'),
       ('Sarah', 'Brown', '09:30:00', '17:30:00'),
       ('Michael', 'Davis', '08:00:00', '16:00:00');

-- Link teachers with subjects
INSERT INTO teacher_subjects (teacher_id, subject_id)
VALUES 
    (1, 1), (1, 4), (1, 7),  -- John Smith teaches Mathematics, Physics, Chemistry for level 1
    (1, 2), (1, 5), (1, 8),  -- John Smith teaches Mathematics, Physics, Chemistry for level 2
    (1, 3), (1, 6), (1, 9),  -- John Smith teaches Mathematics, Physics, Chemistry for level 3
    (2, 10), (2, 13), (2, 16),  -- Mary Johnson teaches Biology, Computer Science, English for level 1
    (2, 11), (2, 14), (2, 17),  -- Mary Johnson teaches Biology, Computer Science, English for level 2
    (2, 12), (2, 15), (2, 18),  -- Mary Johnson teaches Biology, Computer Science, English for level 3
    (3, 19), (3, 22), (3, 25),  -- Robert Williams teaches History, Geography, Art for level 1
    (3, 20), (3, 23), (3, 26),  -- Robert Williams teaches History, Geography, Art for level 2
    (3, 21), (3, 24), (3, 27);  -- Robert Williams teaches History, Geography, Art for level 3

-- Insert students with levels
INSERT INTO students (name, surname, level)
VALUES 
    ('Alice', 'Anderson', 1),
    ('Bob', 'Baker', 1),
    ('Charlie', 'Clark', 1),
    ('Diana', 'Davis', 1),
    ('Edward', 'Evans', 1),
    ('Fiona', 'Fisher', 2),
    ('George', 'Green', 2),
    ('Helen', 'Hill', 2),
    ('Ian', 'Irwin', 2),
    ('Julia3', 'Jones3', 3),
    ('Helen3', 'Hill3', 3),
    ('Ian3', 'Irwin3', 3),
    ('Julia3', 'Jones3', 3);

-- Link students with subjects (each student has exactly 9 subjects)
WITH student_subjects_temp AS (
    SELECT s.id as student_id, s.level as student_level,
           sub.id as subject_id, sub.level as subject_level
    FROM students s
    CROSS JOIN subjects sub
    WHERE s.level = sub.level
)
INSERT INTO student_subjects (student_id, subject_id)
SELECT student_id, subject_id
FROM student_subjects_temp;