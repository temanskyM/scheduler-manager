package com.example.service.excel.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.db.Student;
import com.example.dto.ScheduledLesson;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class StudentScheduleReportBuilder extends AbstractExcelReportBuilder {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] buildReport(List<ScheduledLesson> schedule, List<Student> students) {
        fillSheet(schedule, students);

        try {
            return writeToByteArray(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate student schedule report", e);
        }
    }

    private void fillSheet(List<ScheduledLesson> schedule, List<Student> students) {
        Sheet sheet = workbook.createSheet("Student Schedule");

        // Group schedule by student
        Map<Long, List<ScheduledLesson>> scheduleByStudent = schedule.stream()
                .collect(Collectors.groupingBy(lesson -> lesson.getStudentIds().get(0)));

        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Date", "Time", "Subject", "Teacher", "Classroom"};
        createHeaderRow(headerRow, headers);

        // Add data for each student
        for (Student student : students) {
            rowNum = addStudentData(sheet, student, scheduleByStudent.get(student.getId()), rowNum);
        }

        // Auto-size columns
        autoSizeColumns(sheet, headers.length);
        autoSizeRows(sheet, 0, 2 + LESSONS_PER_DAY - 1); // From week header to last time slot
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

    private int addStudentData(Sheet sheet, Student student, List<ScheduledLesson> studentLessons, int rowNum) {
        // Add student name as a merged cell
        Row studentRow = sheet.createRow(rowNum++);
        Cell studentCell = studentRow.createCell(0);
        studentCell.setCellValue(student.getName() + " " + student.getSurname());
        studentCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        // Add student's lessons
        if (studentLessons != null) {
            for (ScheduledLesson lesson : studentLessons) {
                Row lessonRow = sheet.createRow(rowNum++);
                addLessonData(lessonRow, lesson);
            }
        }
        return rowNum + 1; // Add empty row between students
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

        // Teacher
        Cell teacherCell = row.createCell(col++);
        teacherCell.setCellValue(lesson.getTeacherName() + " " + lesson.getTeacherSurname());
        teacherCell.setCellStyle(dataStyle);

        // Classroom
        Cell classroomCell = row.createCell(col);
        classroomCell.setCellValue(lesson.getClassroomName());
        classroomCell.setCellStyle(dataStyle);
    }
} 