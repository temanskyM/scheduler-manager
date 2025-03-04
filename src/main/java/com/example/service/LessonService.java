package com.example.service;

import com.example.db.Lesson;
import com.example.db.LessonRepository;
import com.example.db.Student;
import com.example.dto.StudentScheduleDto;
import com.example.dto.TeacherScheduleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LessonService {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    public List<Lesson> findAll() {
        return lessonRepository.findAll();
    }
    
    public Optional<Lesson> findById(Long id) {
        return lessonRepository.findById(id);
    }
    
    public Lesson save(Lesson lesson) {
        return lessonRepository.save(lesson);
    }
    
    public void deleteById(Long id) {
        lessonRepository.deleteById(id);
    }

    public List<StudentScheduleDto> getStudentSchedule(Long studentId) {

        // Get lessons for this student from the database
        List<Lesson> lessons = lessonRepository.findByStudentIdOrderByDateStart(studentId);

        // Convert lessons to StudentScheduleDto
        return lessons.stream()
                .map(lesson -> {
                    StudentScheduleDto dto = new StudentScheduleDto();
                    dto.setStart(lesson.getDateStart());
                    dto.setEnd(lesson.getDateEnd());
                    dto.setSubjectName(lesson.getSubject().getName());
                    dto.setSubjectLevel(lesson.getSubject().getLevel());
                    dto.setTeacherName(lesson.getTeacher().getName() + " " + lesson.getTeacher().getSurname());
                    dto.setClassroomName(lesson.getClassroom().getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    public List<TeacherScheduleDto> getTeacherSchedule(Long teacherId) {
        // Get lessons for this student from the database
        List<Lesson> lessons = lessonRepository.findByTeacher_IdOrderByDateStart(teacherId);

        // Convert lessons to StudentScheduleDto
        return lessons.stream()
                .map(lesson -> {
                    TeacherScheduleDto dto = new TeacherScheduleDto();
                    dto.setStart(lesson.getDateStart());
                    dto.setEnd(lesson.getDateEnd());
                    dto.setSubjectName(lesson.getSubject().getName());
                    dto.setSubjectLevel(lesson.getSubject().getLevel());
                    dto.setTeacherName(lesson.getTeacher().getName() + " " + lesson.getTeacher().getSurname());
                    dto.setClassroomName(lesson.getClassroom().getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 