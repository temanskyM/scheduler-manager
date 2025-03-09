package com.example.service.excel.builder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.db.Classroom;
import com.example.dto.ScheduledLesson;
import static com.example.service.schedule.ClassroomScheduler.LESSONS_PER_DAY;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ClassroomScheduleReportBuilder extends AbstractExcelReportBuilder {
    public byte[] buildReport(List<ScheduledLesson> schedule, List<Classroom> classrooms) {
        // Group schedule by classroom
        Map<Long, List<ScheduledLesson>> scheduleByClassroom = schedule.stream()
                .collect(Collectors.groupingBy(ScheduledLesson::getClassroomId));

        // Create a sheet for each classroom
        for (Classroom classroom : classrooms) {
            createClassroomSheet(classroom, scheduleByClassroom.getOrDefault(classroom.getId(), List.of()));
        }

        try {
            return writeToByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate classroom schedule report", e);
        }
    }

    private void createClassroomSheet(Classroom classroom, List<ScheduledLesson> lessons) {
        Sheet sheet = workbook.createSheet(classroom.getName());

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

        // Auto-size columns
        autoSizeColumns(sheet, 6);
        autoSizeRows(sheet, 0, 2 + LESSONS_PER_DAY - 1); // From week header to last time slot
    }

    private void addLessonToSheet(Sheet sheet, ScheduledLesson lesson) {
        // Calculate day and time slot
        int day = lesson.getDateStart().getDayOfWeek().getValue(); // Monday = 0
        int timeSlot = calculateTimeSlot(lesson.getDateStart());

        // Create lesson cell
        Row row = sheet.getRow(timeSlot + 2); // +2 because of week header and day headers
        Cell cell = row.createCell(day); // +1 because of time column
        cell.setCellValue(formatLessonInfo(lesson));
        cell.setCellStyle(dataStyle);
    }

    private String formatLessonInfo(ScheduledLesson lesson) {
        return String.format("%s (Level %d)\n%s\n%s",
                lesson.getSubjectName(),
                lesson.getSubjectLevel(),
                lesson.getTeacherName() + " " + lesson.getTeacherSurname(),
                String.join(", ", lesson.getStudentNames()));
    }
} 