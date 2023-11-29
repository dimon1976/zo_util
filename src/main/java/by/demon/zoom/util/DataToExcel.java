package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(DataToExcel.class);
    private static final short DEFAULT_COLUMN_WIDTH = 15;
    private static final String PATTERN = "yyyy-MM-dd";
    private static final String PATTERN_NUMBER = "0.#";
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();

    static {
        symbols.setDecimalSeparator(',');
    }

    public void exportToExcel(List<String> headers, List<List<Object>> dataset, OutputStream out, short skip) {
        exportObjectToExcel(headers, dataset, out, skip);
    }

    /**
     * @param headers Заголовок
     * @param dataset Коллекция
     * @param out     Выходной поток
     * @param skip    Количество строк для пропуска
     */

    public void exportToExcel(List<String> headers, Collection<T> dataset, OutputStream out, int skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(Globals.SHEET_NAME);
            sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle cellStyle = createCellStyle(workbook);

            createHeaderRow(sheet, headers, headerStyle);

            Iterator<T> iterator = dataset.iterator();
            skipRows(iterator, skip);

            populateDataRows(sheet, iterator, cellStyle);

            workbook.write(out);
        } catch (IOException e) {
            LOG.error("Error while exporting Excel: {}", e.getMessage());
            throw new RuntimeException("Error while exporting Excel", e);
        }
    }

    private void exportObjectToExcel(List<String> headers, List<List<Object>> dataset, OutputStream out, int skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(Globals.SHEET_NAME);
            sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle cellStyle = createCellStyle(workbook);

            createHeaderRow(sheet, headers, headerStyle);

            int rowNum = 0;
            Iterator<List<Object>> iterator = dataset.iterator();
            skipRows(iterator, skip);
            while (iterator.hasNext()) {
                List<Object> rowData = iterator.next();
                XSSFRow row = sheet.createRow(++rowNum);
                int colNum = 0;
                for (Object value : rowData) {
                    XSSFCell cell = row.createCell(colNum++);
                    setCellValue(cell, value, cellStyle);
                }
            }

            workbook.write(out);
        } catch (IOException e) {
            LOG.error("Error exporting object to Excel: {}", e.getMessage());
            throw new RuntimeException("Error exporting object to Excel", e);
        }
    }

    private static <T> void populateDataRows(XSSFSheet sheet, Iterator<T> iterator, XSSFCellStyle cellStyle) {
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
                    setCellValue(cell, value, cellStyle);
                } catch (Exception e) {
                    LOG.error("Error while processing data row: {}", e.getMessage());
                    throw new RuntimeException("Error while processing data row", e);
                }
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
            DecimalFormat decimalFormat = new DecimalFormat(PATTERN_NUMBER, symbols);
            decimalFormat.setParseBigDecimal(true);
            cell.setCellValue(((Number) value).doubleValue());
            cellStyle.setDataFormat(cell.getCellStyle().getDataFormat());
        } else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(String.valueOf(value));
        }
    }
}
