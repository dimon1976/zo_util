package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExcelReader {
    private static final int MAX_COLUMNS = 250;

    public static List<List<Object>> readExcel(File file) throws IOException {
        String fileName = file.getName();
        if (fileName.endsWith(".xls")) {
            return readExcel2003(file);
        } else if (fileName.endsWith(".xlsx")) {
            return readExcel2007(file);
        } else {
            throw new UnsupportedOperationException("Unsupported file format");
        }
    }

    private static List<List<Object>> readExcel2003(File file) throws IOException {
        List<List<Object>> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new HSSFWorkbook(fis)) {
            processWorkbook(workbook, result);
        } catch (IOException e) {
            log.error("Error reading Excel 2003 file: {}", e.getMessage(), e);
            throw e;
        }
        return result;
    }

    private static List<List<Object>> readExcel2007(File file) throws IOException {
        List<List<Object>> result = new ArrayList<>();
        // https://stackoverflow.com/a/75830526
        IOUtils.setByteArrayMaxOverride(1000000000);
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            processWorkbook(workbook, result);
        } catch (IOException e) {
            log.error("Error reading Excel 2007 file: {}", e.getMessage(), e);
            throw e;
        }
        return result;
    }

    private static void processWorkbook(Workbook workbook, List<List<Object>> result) {
        for (Sheet sheet : workbook) {
            processSheet(sheet, result);
        }
    }

    private static void processSheet(Sheet sheet, List<List<Object>> result) {
        for (Row row : sheet) {
            List<Object> rowValues = new ArrayList<>();
            processRow(row, rowValues);
            result.add(rowValues);
        }
    }

    public static void processRow(Row row, List<Object> rowValues) {
        int columnCount = Math.min(row.getLastCellNum(), MAX_COLUMNS);
        for (int colNum = 0; colNum < columnCount; colNum++) {
            Cell cell = row.getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            rowValues.add(getCellValue(cell));
        }
    }


    public static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                /*
                 if (DateUtil.isCellDateFormatted(cell)) {
                 return cell.getDateCellValue();
                 } else {
                 return cell.getNumericCellValue();
                 }
                 */
                double numericValue = cell.getNumericCellValue();
                long longValue = (long) numericValue; // Проверяем, является ли значение целым числом
                if (numericValue == longValue) {
                    return longValue; // Возвращаем целочисленное значение
                } else {
                    DecimalFormat decimalFormat = new DecimalFormat("#,###.#####"); // Форматирование числа с разделителем ","
                    return decimalFormat.format(numericValue); // Возвращаем числовое значение с десятичной частью
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
