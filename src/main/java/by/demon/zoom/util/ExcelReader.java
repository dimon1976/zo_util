package by.demon.zoom.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


@Service
public class ExcelReader {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelReader.class);
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
            LOG.error("Error reading Excel 2003 file: {}", e.getMessage(), e);
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
            LOG.error("Error reading Excel 2007 file: {}", e.getMessage(), e);
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
                return cell.getStringCellValue().replaceAll(";", "|");
            case NUMERIC:
                return formatNumericValue(cell.getNumericCellValue());
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static Object formatNumericValue(double numericValue) {
        long longValue = (long) numericValue;
        if (numericValue == longValue) {
            return longValue;
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#,###.#####");
            return decimalFormat.format(numericValue);
        }
    }
}
