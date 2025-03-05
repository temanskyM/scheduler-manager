package com.example.service.excel.builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class AbstractExcelReportBuilder {
    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    protected static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    protected static final int LESSON_DURATION_MINUTES = 45;
    protected static final int BREAK_DURATION_MINUTES = 10;
    protected static final int TOTAL_SLOT_DURATION = LESSON_DURATION_MINUTES + BREAK_DURATION_MINUTES;
    protected static final int LESSONS_PER_DAY = 10;
    protected static final LocalDateTime SCHOOL_START_TIME = LocalDateTime.of(2000, 1, 1, 8, 0);

    protected final Workbook workbook;
    protected final CellStyle headerStyle;
    protected final CellStyle dataStyle;
    protected final CellStyle dayHeaderStyle;

    public AbstractExcelReportBuilder() {
        this.workbook = new XSSFWorkbook();
        this.headerStyle = createHeaderStyle();
        this.dataStyle = createDataStyle();
        this.dayHeaderStyle = createDayHeaderStyle();
    }

    private CellStyle createHeaderStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDayHeaderStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    protected void createWeekHeader(Sheet sheet, LocalDateTime weekStart, int startCol, int endCol) {
        Row weekHeaderRow = sheet.createRow(0);
        Cell weekHeaderCell = weekHeaderRow.createCell(startCol);
        weekHeaderCell.setCellValue("Week of " + weekStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        weekHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, endCol));
    }

    protected void createDayHeaders(Sheet sheet, LocalDateTime weekStart, int startRow) {
        Row dayHeaderRow = sheet.createRow(startRow);
        for (int i = 0; i < 5; i++) {
            LocalDateTime currentDay = weekStart.plusDays(i);
            Cell dayCell = dayHeaderRow.createCell(i + 1);
            dayCell.setCellValue(currentDay.format(DAY_FORMATTER));
            dayCell.setCellStyle(dayHeaderStyle);
        }
    }

    protected void createTimeSlots(Sheet sheet, int startRow) {
        for (int i = 0; i < LESSONS_PER_DAY; i++) {
            Row timeRow = sheet.createRow(startRow + i);
            LocalDateTime time = SCHOOL_START_TIME.plusMinutes((long) i * TOTAL_SLOT_DURATION);
            Cell timeCell = timeRow.createCell(0);
            timeCell.setCellValue(time.format(TIME_FORMATTER));
            timeCell.setCellStyle(dataStyle);
        }
    }

    protected void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    protected byte[] writeToByteArray() throws java.io.IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}
