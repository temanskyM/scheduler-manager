package com.example.controller;

import java.util.List;

import com.example.db.Student;
import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import com.example.service.ExcelReportService;
import com.example.service.SchedulingService;
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
    private final SchedulingService schedulingService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @GetMapping("/student-schedule")
    public ResponseEntity<byte[]> downloadStudentSchedule() {
        // Get the current week's lessons from the database
        List<ScheduledLesson> schedule = schedulingService.getCurrentWeekLessons();
        List<Student> students = studentService.findAll();

        byte[] excelContent = excelReportService.generateStudentScheduleReport(schedule, students);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.add("Content-Disposition",
                "attachment; filename=\"student_schedule.xlsx\"; filename*=UTF-8''student_schedule.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }

    @GetMapping("/teacher-schedule")
    public ResponseEntity<byte[]> downloadTeacherSchedule() {
        // Get the current week's lessons from the database
        List<ScheduledLesson> schedule = schedulingService.getCurrentWeekLessons();
        List<Teacher> teachers = teacherService.findAll();

        byte[] excelContent = excelReportService.generateTeacherScheduleReport(schedule, teachers);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.add("Content-Disposition",
                "attachment; filename=\"teacher_schedule.xlsx\"; filename*=UTF-8''teacher_schedule.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
} 