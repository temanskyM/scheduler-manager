package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.db.Lesson;
import com.example.db.LessonRepository;
import com.example.db.Student;
import com.example.dto.ScheduledLesson;
import com.example.dto.StudentScheduleDto;
import com.example.dto.TeacherScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    
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

    public List<ScheduledLesson> getCurrentWeekLessons() {
        LocalDateTime weekStart = getCurrentWeekStart();
        LocalDateTime weekEnd = weekStart.plusDays(5);

        return lessonRepository.findByDateStartBetweenOrderByDateStart(weekStart, weekEnd).stream()
                .map(this::convertToScheduledLesson)
                .collect(Collectors.toList());
    }

    private LocalDateTime getCurrentWeekStart() {
        LocalDateTime now = LocalDateTime.now();
        return now.with(DayOfWeek.MONDAY).withHour(8).withMinute(0).withSecond(0).withNano(0);
    }

    private ScheduledLesson convertToScheduledLesson(Lesson lesson) {
        ScheduledLesson scheduledLesson = new ScheduledLesson();
        scheduledLesson.setDateStart(lesson.getDateStart());
        scheduledLesson.setDateEnd(lesson.getDateEnd());
        scheduledLesson.setClassroomId(lesson.getClassroom().getId());
        scheduledLesson.setClassroomName(lesson.getClassroom().getName());
        scheduledLesson.setTeacherId(lesson.getTeacher().getId());
        scheduledLesson.setTeacherName(lesson.getTeacher().getName());
        scheduledLesson.setTeacherSurname(lesson.getTeacher().getSurname());
        scheduledLesson.setSubjectId(lesson.getSubject().getId());
        scheduledLesson.setSubjectName(lesson.getSubject().getName());
        scheduledLesson.setSubjectLevel(lesson.getSubject().getLevel());
        scheduledLesson.setStudentIds(lesson.getStudents().stream()
                .map(Student::getId)
                .collect(Collectors.toList()));
        scheduledLesson.setStudentNames(lesson.getStudents().stream()
                .map(s -> s.getName() + " " + s.getSurname())
                .collect(Collectors.toList()));
        return scheduledLesson;
    }
} 