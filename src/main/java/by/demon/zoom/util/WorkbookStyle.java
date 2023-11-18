package by.demon.zoom.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Service
public class WorkbookStyle {

    private static final Logger LOG = LoggerFactory.getLogger(WorkbookStyle.class);
    private static final String PATTERN_NUMBER = "0.#";
    private static final String DEFAULT_FONT_NAME = "Calibri";
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();

    static {
        symbols.setDecimalSeparator(',');
    }

    public static XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(headerStyle);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);

        XSSFFont headerFont = workbook.createFont();
        headerFont.setFontName(DEFAULT_FONT_NAME);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        return headerStyle;
    }

    public static XSSFCellStyle createCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont cellFont = workbook.createFont();
        cellFont.setBold(false);
        cellStyle.setFont(cellFont);

        return cellStyle;
    }

    public static void createHeaderRow(XSSFSheet sheet, List<String> header, XSSFCellStyle headerStyle) {
        if (header != null && !header.isEmpty()) {
            XSSFRow row = sheet.createRow(0);
            for (int i = 0; i < header.size(); i++) {
                XSSFCell cell = row.createCell(i);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(header.get(i));
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, header.size() - 1));
        }
    }

    public static void skipRows(Iterator<?> iterator, int skip) {
        for (int i = 0; i < skip; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            } else {
                break;
            }
        }
    }

    public static <T extends Object> void populateDataRows(XSSFSheet sheet, Iterator<T> iterator, XSSFCellStyle cellStyle, String pattern) {
        int rowIndex = 0;
        while (iterator.hasNext()) {
            T data = iterator.next();
            Field[] fields = data.getClass().getDeclaredFields();
            XSSFRow row = sheet.createRow(++rowIndex);
            for (int i = 0; i < fields.length; i++) {
                XSSFCell cell = row.createCell(i);
                cell.setCellStyle(cellStyle);
                Field field = fields[i];
                String fieldName = field.getName();
                String getMethodName = "get" + capitalize(fieldName);
                try {
                    Method getMethod = data.getClass().getMethod(getMethodName);
                    Object value = getMethod.invoke(data);
                    setCellValue(cell, value, cellStyle, pattern);
                } catch (Exception e) {
                    LOG.error("Error while processing data row: {}", e.getMessage());
                    throw new RuntimeException("Error while processing data row", e);
                }
            }
        }
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void setCellValue(XSSFCell cell, Object value, XSSFCellStyle cellStyle, String pattern) {
        cell.setCellStyle(cellStyle);
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Boolean) {
            cell.setCellValue((boolean) value);
        } else if (value instanceof Date) {
            if (pattern != null && !pattern.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                cell.setCellValue(sdf.format((Date) value));
            } else {
                cell.setCellValue((Date) value);
            }
        } else if (value instanceof byte[]) {
            cell.getRow().setHeightInPoints(60);
            cell.getSheet().setColumnWidth(cell.getColumnIndex(), (short) (35.7 * 80));
        } else if (value instanceof Number) {
            DecimalFormat decimalFormat = new DecimalFormat(PATTERN_NUMBER, symbols);
            decimalFormat.setParseBigDecimal(true);
            cell.setCellValue(((Number) value).doubleValue());
            cellStyle.setDataFormat(cell.getCellStyle().getDataFormat());
        } else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(String.valueOf(value));
        }
    }

    public static void setBorder(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
    }
}
