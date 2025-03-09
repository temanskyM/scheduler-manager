-- Add unique constraints to lessons table
ALTER TABLE lessons
    ADD CONSTRAINT unique_classroom_date_start UNIQUE (classroom_id, date_start),
    ADD CONSTRAINT unique_classroom_teacher_date_start UNIQUE (classroom_id, teacher_id, date_start),
    ADD CONSTRAINT unique_classroom_subject_date_start UNIQUE (classroom_id, subject_id, date_start); 