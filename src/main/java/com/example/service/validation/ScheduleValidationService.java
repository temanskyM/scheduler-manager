package com.example.service.validation;

import java.util.List;
import java.util.Set;

import com.example.db.Student;
import com.example.db.Subject;
import com.example.db.Teacher;
import com.example.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class ScheduleValidationService {

    public void validateScheduleRequirements(List<Teacher> teachers, List<Student> students, List<Subject> subjects) {
        validateTeacherSubjects(teachers);
        validateStudentSubjects(students);
        validateSubjectTeachers(subjects);
    }

    private void validateTeacherSubjects(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            if (teacher.getSubjects() == null || teacher.getSubjects().isEmpty()) {
                throw new ValidationException(
                        String.format("Teacher %s %s must have at least one subject assigned",
                                teacher.getName(), teacher.getSurname()));
            }
        }
    }

    private void validateStudentSubjects(List<Student> students) {
        for (Student student : students) {
            Set<Subject> studentSubjects = student.getSubjects();
            if (studentSubjects == null || studentSubjects.size() != 9) {
                throw new ValidationException(
                        String.format("Student %s %s must have exactly 9 subjects assigned. Current count: %d",
                                student.getName(), student.getSurname(),
                                studentSubjects != null ? studentSubjects.size() : 0));
            }
        }
    }

    private void validateSubjectTeachers(List<Subject> subjects) {
        for (Subject subject : subjects) {
            if (subject.getTeachers() == null || subject.getTeachers().isEmpty()) {
                throw new ValidationException(
                        String.format("Subject %s must have at least one teacher assigned",
                                subject.getName()));
            }
        }
    }
} 