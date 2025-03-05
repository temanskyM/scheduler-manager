package com.example.service.excel.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.db.Classroom;
import com.example.dto.ScheduledLesson;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class ClassroomScheduleReportBuilder extends AbstractExcelReportBuilder {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] buildReport(List<ScheduledLesson> schedule, List<Classroom> classrooms) {
        fillSheet(schedule, classrooms);

        try {
            return writeToByteArray(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate classroom schedule report", e);
        }
    }

    private void fillSheet(List<ScheduledLesson> schedule, List<Classroom> classrooms) {
        Sheet sheet = workbook.createSheet("Classroom Schedule");
        // Group schedule by classroom
        Map<Long, List<ScheduledLesson>> scheduleByClassroom = schedule.stream()
                .collect(Collectors.groupingBy(ScheduledLesson::getClassroomId));

        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Date", "Time", "Subject", "Level", "Teacher", "Students"};
        createHeaderRow(headerRow, headers);

        // Add data for each classroom
        for (Classroom classroom : classrooms) {
            rowNum = addClassroomData(sheet, classroom, scheduleByClassroom.get(classroom.getId()), rowNum);
        }

        // Auto-size columns
        autoSizeColumns(sheet, headers.length);
    }

    private void createHeaderRow(Row headerRow, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private int addClassroomData(Sheet sheet, Classroom classroom, List<ScheduledLesson> classroomLessons, int rowNum) {
        // Add classroom name as a merged cell
        Row classroomRow = sheet.createRow(rowNum++);
        Cell classroomCell = classroomRow.createCell(0);
        classroomCell.setCellValue(classroom.getName());
        classroomCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

        // Add classroom's lessons
        if (classroomLessons != null) {
            for (ScheduledLesson lesson : classroomLessons) {
                Row lessonRow = sheet.createRow(rowNum++);
                addLessonData(lessonRow, lesson);
            }
        }
        return rowNum + 1; // Add empty row between classrooms
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

        // Teacher
        Cell teacherCell = row.createCell(col++);
        teacherCell.setCellValue(lesson.getTeacherName() + " " + lesson.getTeacherSurname());
        teacherCell.setCellStyle(dataStyle);

        // Students
        Cell studentsCell = row.createCell(col);
        studentsCell.setCellValue(String.join(", ", lesson.getStudentNames()));
        studentsCell.setCellStyle(dataStyle);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] writeToByteArray(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
} 