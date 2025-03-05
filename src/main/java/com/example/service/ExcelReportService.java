package com.example.service;

import java.util.List;

import com.example.db.Classroom;
import com.example.db.Student;
import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import com.example.service.excel.builder.ClassroomScheduleReportBuilder;
import com.example.service.excel.builder.StudentScheduleReportBuilder;
import com.example.service.excel.builder.TeacherScheduleReportBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelReportService {
    public byte[] generateStudentScheduleReport(List<ScheduledLesson> schedule, List<Student> students) {
        StudentScheduleReportBuilder builder = new StudentScheduleReportBuilder();
        return builder.buildReport(schedule, students);
    }

    public byte[] generateTeacherScheduleReport(List<ScheduledLesson> schedule, List<Teacher> teachers) {
        TeacherScheduleReportBuilder builder = new TeacherScheduleReportBuilder();
        return builder.buildReport(schedule, teachers);
    }

    public byte[] generateClassroomScheduleReport(List<ScheduledLesson> schedule, List<Classroom> classrooms) {
        ClassroomScheduleReportBuilder builder = new ClassroomScheduleReportBuilder();
        return builder.buildReport(schedule, classrooms);
    }
} 