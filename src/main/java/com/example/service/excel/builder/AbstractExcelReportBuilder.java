package com.example.service.excel.builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.example.service.schedule.ClassroomScheduler.LESSONS_PER_DAY;
import static com.example.service.schedule.ClassroomScheduler.SCHOOL_START_TIME;
import static com.example.service.schedule.ClassroomScheduler.TOTAL_SLOT_DURATION;
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
        style.setWrapText(true); // Enable text wrapping
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
            LocalTime time = SCHOOL_START_TIME.plusMinutes((long) i * TOTAL_SLOT_DURATION);
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

    protected void autoSizeRows(Sheet sheet, int startRow, int endRow) {
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                // Calculate the maximum height needed for this row
                int maxLines = 0;
                for (Cell cell : row) {
                    if (cell != null) {
                        int numLines = cell.getStringCellValue().split("\n").length;
                        maxLines = Math.max(maxLines, numLines); // 15 points per line
                    }
                }
                // Set the row height to accommodate the content
                row.setHeight((short) ((short) maxLines * sheet.getDefaultRowHeight()));
            }
        }
    }

    protected byte[] writeToByteArray() throws java.io.IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }

    protected int calculateTimeSlot(LocalDateTime lessonTime) {
        return ((lessonTime.getHour() * 60 + lessonTime.getMinute() -
                (SCHOOL_START_TIME.getHour() * 60 + SCHOOL_START_TIME.getMinute())) / TOTAL_SLOT_DURATION);
    }
}
