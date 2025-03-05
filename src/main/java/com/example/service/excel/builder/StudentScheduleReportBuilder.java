package com.example.service.excel.builder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.db.Student;
import com.example.dto.ScheduledLesson;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class StudentScheduleReportBuilder extends AbstractExcelReportBuilder {
    private static final String SHEET_NAME_FORMAT = "Class %s, %s %s";

    public byte[] buildReport(List<ScheduledLesson> schedule, List<Student> students) {

        // Group schedule by studentId
        Map<Long, List<ScheduledLesson>> scheduleByStudent = getGroupedLessonsByStudentId(schedule);

        // Create a sheet for each student
        for (Student student : students) {
            createStudentSheet(student, scheduleByStudent.getOrDefault(student.getId(), List.of()));
        }

        try {
            return writeToByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate student schedule report", e);
        }
    }

    private static Map<Long, List<ScheduledLesson>> getGroupedLessonsByStudentId(
            List<ScheduledLesson> schedule) {
        Map<Long, List<ScheduledLesson>> scheduleByStudentId = new HashMap<>();
        for (ScheduledLesson lesson : schedule) {
            for (Long studentId : lesson.getStudentIds()) {
                scheduleByStudentId.compute(studentId,
                        (key, value) -> {
                            if (value == null) {
                                ArrayList<ScheduledLesson> lessons = new ArrayList<>();
                                lessons.add(lesson);
                                return lessons;
                            } else {
                                value.add(lesson);
                                return value;
                            }
                        });
            }
        }
        return scheduleByStudentId;
    }

    private void createStudentSheet(Student student, List<ScheduledLesson> lessons) {
        String sheetName = SHEET_NAME_FORMAT.formatted(student.getLevel(), student.getName(), student.getSurname());
        Sheet sheet = workbook.createSheet(sheetName);

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

    private int calculateTimeSlot(LocalDateTime lessonTime) {
        return ((lessonTime.getHour() * 60 + lessonTime.getMinute() -
                (SCHOOL_START_TIME.getHour() * 60 + SCHOOL_START_TIME.getMinute())) / TOTAL_SLOT_DURATION);
    }

    private String formatLessonInfo(ScheduledLesson lesson) {
        return String.format("%s (Level %d)\n%s\n%s %s",
                lesson.getSubjectName(),
                lesson.getSubjectLevel(),
                lesson.getClassroomName(),
                lesson.getTeacherName(),
                lesson.getTeacherSurname());
    }
} 