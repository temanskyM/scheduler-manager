package com.example.service.excel.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.db.Teacher;
import com.example.dto.ScheduledLesson;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class TeacherScheduleReportBuilder extends AbstractExcelReportBuilder {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] buildReport(List<ScheduledLesson> schedule, List<Teacher> teachers) {
        fillSheet(schedule, teachers);

        try {
            return writeToByteArray(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate teacher schedule report", e);
        }
    }

    private void fillSheet(List<ScheduledLesson> schedule, List<Teacher> teachers) {
        Sheet sheet = workbook.createSheet("Teacher Schedule");

        // Group schedule by teacher
        Map<Long, List<ScheduledLesson>> scheduleByTeacher = schedule.stream()
                .collect(Collectors.groupingBy(ScheduledLesson::getTeacherId));

        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Date", "Time", "Subject", "Level", "Classroom", "Students"};
        createHeaderRow(headerRow, headers);

        // Add data for each teacher
        for (Teacher teacher : teachers) {
            rowNum = addTeacherData(sheet, teacher, scheduleByTeacher.get(teacher.getId()), rowNum);
        }

        // Auto-size columns
        autoSizeColumns(sheet, headers.length);
    }

    private byte[] writeToByteArray(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }

    private void createHeaderRow(Row headerRow, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private int addTeacherData(Sheet sheet, Teacher teacher, List<ScheduledLesson> teacherLessons, int rowNum) {
        // Add teacher name as a merged cell
        Row teacherRow = sheet.createRow(rowNum++);
        Cell teacherCell = teacherRow.createCell(0);
        teacherCell.setCellValue(teacher.getName() + " " + teacher.getSurname());
        teacherCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

        // Add teacher's lessons
        if (teacherLessons != null) {
            for (ScheduledLesson lesson : teacherLessons) {
                Row lessonRow = sheet.createRow(rowNum++);
                addLessonData(lessonRow, lesson);
            }
        }
        return rowNum + 1; // Add empty row between teachers
    }

    private void addLessonData(Row row, ScheduledLesson lesson) {
        int col = 0;

        // Date
        Cell dateCell = row.createCell(col++);
        dateCell.setCellValue(lesson.getDateStart().format(DATE_FORMATTER));
        dateCell.setCellStyle(dataStyle);

        // Time
        Cell timeCell = row.createCell(col++);
        timeCell.setCellValue(lesson.getDateStart().format(TIME_FORMATTER) + " - " +
                lesson.getDateEnd().format(TIME_FORMATTER));
        timeCell.setCellStyle(dataStyle);

        // Subject
        Cell subjectCell = row.createCell(col++);
        subjectCell.setCellValue(lesson.getSubjectName());
        subjectCell.setCellStyle(dataStyle);

        // Level
        Cell levelCell = row.createCell(col++);
        levelCell.setCellValue(lesson.getSubjectLevel());
        levelCell.setCellStyle(dataStyle);

        // Classroom
        Cell classroomCell = row.createCell(col++);
        classroomCell.setCellValue(lesson.getClassroomName());
        classroomCell.setCellStyle(dataStyle);

        // Students
        Cell studentsCell = row.createCell(col);
        studentsCell.setCellValue(String.join(", ", lesson.getStudentNames()));
        studentsCell.setCellStyle(dataStyle);
    }
} 