package com.example.service.excel.builder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import static com.example.service.schedule.ClassroomScheduler.LESSONS_PER_DAY;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class TeacherScheduleReportBuilder extends AbstractExcelReportBuilder {
    public byte[] buildReport(List<ScheduledLesson> schedule, List<Teacher> teachers) {
        // Group schedule by teacher
        Map<Long, List<ScheduledLesson>> scheduleByTeacher = schedule.stream()
                .collect(Collectors.groupingBy(ScheduledLesson::getTeacherId));

        // Create a sheet for each teacher
        for (Teacher teacher : teachers) {
            createTeacherSheet(teacher, scheduleByTeacher.getOrDefault(teacher.getId(), List.of()));
        }

        try {
            return writeToByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate teacher schedule report", e);
        }
    }

    private void createTeacherSheet(Teacher teacher, List<ScheduledLesson> lessons) {
        Sheet sheet = workbook.createSheet(teacher.getName() + " " + teacher.getSurname());

        // Create week header
        LocalDateTime weekStart = lessons.stream()
                .map(ScheduledLesson::getDateStart)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        createWeekHeader(sheet, weekStart, 0, 5);

        // Create day headers
        createDayHeaders(sheet, weekStart, 1);

        // Create time slots
        createTimeSlots(sheet, 2);

        // Add lessons
        for (ScheduledLesson lesson : lessons) {
            addLessonToSheet(sheet, lesson);
        }

        // Auto-size columns and rows
        autoSizeColumns(sheet, 6);
        autoSizeRows(sheet, 0, 2 + LESSONS_PER_DAY - 1); // From week header to last time slot
    }

    private void addLessonToSheet(Sheet sheet, ScheduledLesson lesson) {
        // Calculate day and time slot
        int day = lesson.getDateStart().getDayOfWeek().getValue() - 1; // Monday = 0
        int timeSlot = calculateTimeSlot(lesson.getDateStart());

        // Create lesson cell
        Row row = sheet.getRow(timeSlot + 2); // +2 because of week header and day headers
        Cell cell = row.createCell(day + 1); // +1 because of time column
        cell.setCellValue(formatLessonInfo(lesson));
        cell.setCellStyle(dataStyle);
    }

    private String formatLessonInfo(ScheduledLesson lesson) {
        return String.format("%s (Level %d)\n%s\n%s",
                lesson.getSubjectName(),
                lesson.getSubjectLevel(),
                lesson.getClassroomName(),
                String.join(", ", lesson.getStudentNames()));
    }
} 