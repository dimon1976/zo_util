package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static by.demon.zoom.util.WorkbookStyle.*;

@Slf4j
@Service
public class DataToExcel<T> {

    private static final short DEFAULT_COLUMN_WIDTH = 15;
    private static final String PATTERN = "yyyy-MM-dd";
    private static final String PATTERN_NUMBER = "0.#";
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();

    static {
        symbols.setDecimalSeparator(',');
    }

    public void exportToExcel(List<String> headers, Collection<T> dataset, OutputStream out, int skip) {
        export(headers, dataset.iterator(), out, skip);
    }

    public void exportToExcel(List<String> headers, List<List<Object>> dataset, OutputStream out, int skip) {
        export(headers, dataset.iterator(), out, skip);
    }

    private <E> void export(List<String> headers, Iterator<E> iterator, OutputStream out, int skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = createSheetWithHeader(workbook, headers);
            XSSFCellStyle cellStyle = createCellStyle(workbook);

            skipRows(iterator, skip);
            populateDataRows(sheet, iterator, cellStyle);

            workbook.write(out);
        } catch (IOException e) {
            log.error("Error while exporting Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error while exporting Excel", e);
        }
    }

    private static <E> void populateDataRows(XSSFSheet sheet, Iterator<E> iterator, XSSFCellStyle cellStyle) {
        int rowIndex = 0;
        while (iterator.hasNext()) {
            E data = iterator.next();
            XSSFRow row = sheet.createRow(++rowIndex);

            if (data instanceof List) {
                populateRowWithList(row, (List<?>) data, cellStyle);
            } else {
                populateRowWithObject(row, data, cellStyle);
            }
        }
    }

    private static void populateRowWithList(XSSFRow row, List<?> rowData, XSSFCellStyle cellStyle) {
        int colNum = 0;
        for (Object value : rowData) {
            XSSFCell cell = row.createCell(colNum++);
            setCellValue(cell, value, cellStyle);
        }
    }

    private static <T> void populateRowWithObject(XSSFRow row, T data, XSSFCellStyle cellStyle) {
        Field[] fields = data.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            XSSFCell cell = row.createCell(i);
            Field field = fields[i];
            String fieldName = field.getName();
            String getMethodName = "get" + capitalize(fieldName);
            try {
                Method getMethod = data.getClass().getMethod(getMethodName);
                Object value = getMethod.invoke(data);
                setCellValue(cell, value, cellStyle);
            } catch (Exception e) {
                log.error("Error while processing data row: {}", e.getMessage(), e);
                throw new RuntimeException("Error while processing data row", e);
            }
        }
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static void setCellValue(XSSFCell cell, Object value, XSSFCellStyle cellStyle) {
        cell.setCellStyle(cellStyle);
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Boolean) {
            cell.setCellValue((boolean) value);
        } else if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
            cell.setCellValue(sdf.format((Date) value));
        } else if (value instanceof byte[]) {
            cell.getRow().setHeightInPoints(60);
            cell.getSheet().setColumnWidth(cell.getColumnIndex(), (short) (35.7 * 80));
        } else if (value instanceof Number) {
            Number number = (Number) value;
            if (number.doubleValue() == 0.0) {
                cell.setBlank();
            } else {
                DecimalFormat decimalFormat = new DecimalFormat(PATTERN_NUMBER, symbols);
                decimalFormat.setParseBigDecimal(true);
                cell.setCellValue(number.doubleValue());
                cellStyle.setDataFormat(cell.getCellStyle().getDataFormat());
            }
        } else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(String.valueOf(value));
        }
    }

    private static void skipRows(Iterator<?> iterator, int skip) {
        for (int i = 0; i < skip && iterator.hasNext(); i++) {
            iterator.next();
        }
    }

    private static XSSFSheet createSheetWithHeader(XSSFWorkbook workbook, List<String> headers) {
        XSSFSheet sheet = workbook.createSheet(Globals.SHEET_NAME);
        sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);

        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(headers.get(i));
        }
        return sheet;
    }
}