package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static by.demon.zoom.util.WorkbookStyle.*;

@Slf4j
@Service
public class DataToExcel<T> {


    private static final Logger LOG = LoggerFactory.getLogger(DataToExcel.class);
    private static final short DEFAULT_COLUMN_WIDTH = 15;



    public void exportToExcel(List<String> headers, Collection<T> dataset, OutputStream out, short skip) {
        exportToExcel(Globals.SHEET_NAME, headers, dataset, out, "yyyy-MM-dd", skip);
    }

    public void exportToExcel(List<String> headers, List<List<Object>> dataset, OutputStream out, short skip) {
        exportObjectToExcel(Globals.SHEET_NAME, headers, dataset, out, "yyyy-MM-dd", skip);
    }

    /**
     * @param title   Title
     * @param headers Заголовок
     * @param dataset Коллекция
     * @param out     Выходной поток
     * @param pattern Паттерн для замены
     */

    private void exportToExcel(String title, List<String> headers, Collection<T> dataset, OutputStream out, String pattern, short skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(title);
            sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle cellStyle = createCellStyle(workbook);

            createHeaderRow(sheet, headers, headerStyle);

            Iterator<T> iterator = dataset.iterator();
            skipRows(iterator, skip);

            populateDataRows(sheet, iterator, cellStyle, pattern);

            workbook.write(out);
        } catch (IOException e) {
            LOG.error("Error while exporting Excel: {}", e.getMessage());
            throw new RuntimeException("Error while exporting Excel", e);
        }
    }

    private void exportObjectToExcel(String title, List<String> headers, List<List<Object>> dataset, OutputStream out, String pattern, short skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(title);
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
                    setCellValue(cell, value, cellStyle, pattern);
                }
            }

            workbook.write(out);
        } catch (IOException e) {
            LOG.error("Error exporting object to Excel: {}", e.getMessage());
            throw new RuntimeException("Error exporting object to Excel", e);
        }
    }


}
