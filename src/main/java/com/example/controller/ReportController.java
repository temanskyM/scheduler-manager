package com.example.controller;

import java.util.List;

import com.example.db.Classroom;
import com.example.db.Student;
import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import com.example.service.ClassroomService;
import com.example.service.ExcelReportService;
import com.example.service.LessonService;
import com.example.service.StudentService;
import com.example.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ExcelReportService excelReportService;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ClassroomService classroomService;

    @GetMapping("/student-schedule")
    public ResponseEntity<byte[]> downloadStudentSchedule() {
        List<Student> students = studentService.findAll();
        List<ScheduledLesson> schedule = lessonService.getCurrentWeekLessons();

        byte[] report = excelReportService.generateStudentScheduleReport(schedule, students);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_schedule.xlsx")
                .body(report);
    }

    @GetMapping("/teacher-schedule")
    public ResponseEntity<byte[]> downloadTeacherSchedule() {
        List<Teacher> teachers = teacherService.findAll();
        List<ScheduledLesson> schedule = lessonService.getCurrentWeekLessons();

        byte[] report = excelReportService.generateTeacherScheduleReport(schedule, teachers);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teacher_schedule.xlsx")
                .body(report);
    }

    @GetMapping("/classroom-schedule")
    public ResponseEntity<byte[]> downloadClassroomSchedule() {
        List<Classroom> classrooms = classroomService.findAll();
        List<ScheduledLesson> schedule = lessonService.getCurrentWeekLessons();

        byte[] report = excelReportService.generateClassroomScheduleReport(schedule, classrooms);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=classroom_schedule.xlsx")
                .body(report);
    }
} 