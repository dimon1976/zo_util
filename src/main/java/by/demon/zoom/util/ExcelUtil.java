package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T>
 * @version 1.0
 */

@Slf4j
@Service
public class ExcelUtil<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelUtil.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String PATTERN_NUMBER = "0.#";
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    private static final short DEFAULT_COLUMN_WIDTH = 15;
    private static final int MAX_COLUMNS = 250;
    private static final String DEFAULT_FONT_NAME = "Calibri";

    static {
        symbols.setDecimalSeparator(',');
    }


    public static List<List<Object>> readExcel(File file) throws IOException {
        String fileName = file.getName();
        String extension = FilenameUtils.getExtension(fileName);
        if (!"xlsx".equals(extension)) {
            throw new IOException("Неподдерживаемый тип файлов");
        }
        return readExcel2007(file);
    }

    public static List<List<Object>> readExcel(InputStream is, String suffix) throws IOException {
        if (!Globals.SUFFIX_XLSX.equals(suffix)) {
            throw new IOException("Неподдерживаемый тип файлов");
        }
        return readExcel2007(is);
    }


    public void download(String filename, InputStream is, HttpServletResponse response) throws IOException {
        if (filename == null) {
            LOG.error("Filename is null. Unable to proceed with download.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
            return;
        }
        LOG.info("Downloading file: {}", filename);
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), StandardCharsets.ISO_8859_1));
        response.setContentType("application/vnd.ms-excel;charset=gb2312");
        try (InputStream fis = new BufferedInputStream(is);
             OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                toClient.write(buffer, 0, bytesRead);
            }
            toClient.flush(); // Flush the output stream after writing all the data
        } catch (IOException ex) {
            LOG.error("Error during file download: {}", ex.getMessage());
            throw ex;
        }
    }


    public void download(String filename, String path, HttpServletResponse response) throws IOException {
        download(filename, new FileInputStream(path), response);
    }


    private static List<List<Object>> readExcel2007(InputStream is) {
        Long now = MethodPerformance.start();
        List<List<Object>> list = new ArrayList<>();
        // https://stackoverflow.com/a/75830526
        IOUtils.setByteArrayMaxOverride(1000000000);
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<Object> objectList = new ArrayList<>();
                getRowList(row, objectList);
                list.add(objectList);
            }
        } catch (IOException e) {
            LOG.error("Ошибка при чтении файла Excel", e);
        }
        MethodPerformance.finish(now, "чтения файла excel");
        return list;
    }

    public static void getRowList(Row row, List<Object> linked) {
        for (int j = 0; j <= MAX_COLUMNS; j++) {
            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell.getCellType() == CellType.FORMULA) {
                linked.add(cell.getCellFormula());
            } else {
                linked.add(getCellValue(cell));
            }

//            if (cell == null) {
//                linked.add("");
//            } else if (cell.getCellType() == CellType.FORMULA) {
//                linked.add(cell.getCellFormula());
//            } else {
//                linked.add(getCellValue(cell));
//            }
        }
    }

    public static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
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

    public static Object getValueFormula(Cell cell) {
        Object value;
        switch (cell.getCachedFormulaResultType()) {
            case STRING:
                String temp = cell.getStringCellValue();
                value = temp.substring(1, temp.length() - 1);
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = ExcelUtil.SDF.format(DateUtil.getJavaDate(cell.getNumericCellValue()));
                } else {
                    value = String.valueOf(cell.getNumericCellValue());
                    value = matchDoneBigDecimal(String.valueOf(value));
                    value = convertNumByReg(String.valueOf(value));
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case BLANK:
                value = "";
                break;
            default:
                value = cell.toString();
        }
        return value;
    }

    public static Object getValue(Cell cell) {
        Object value;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = ExcelUtil.SDF.format(DateUtil.getJavaDate(cell.getNumericCellValue()));
                } else {
                    value = String.valueOf(cell.getNumericCellValue());
                    value = matchDoneBigDecimal(String.valueOf(value));
                    value = convertNumByReg(String.valueOf(value));
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case BLANK:
                value = "";
                break;
            default:
                value = cell.toString();
        }
        return value;
    }

    public static String matchDoneBigDecimal(String bigDecimal) {
        boolean flg = Pattern.matches("^-?\\d+(\\.\\d+)?(E-?\\d+)?$", bigDecimal);
        if (flg) {
            BigDecimal bd = new BigDecimal(bigDecimal);
            bigDecimal = bd.toPlainString();
        }
        return bigDecimal;
    }

    public static String convertNumByReg(String number) {
        Pattern compile = Pattern.compile("^(\\d+)(\\.0*)?$");
        Matcher matcher = compile.matcher(number);
        while (matcher.find()) {
            number = matcher.group(1);
        }
        return number;
    }

    private static List<List<Object>> readExcel2007(File file) throws IOException {
        return readExcel2007(new FileInputStream(file));
    }

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

    public void exportToExcel(String title, List<String> headers, Collection<T> dataset, OutputStream out, String pattern, short skip) {
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

    public void exportObjectToExcel(String title, List<String> headers, List<List<Object>> dataset, OutputStream out, String pattern, short skip) {
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

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
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

    private XSSFCellStyle createCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont cellFont = workbook.createFont();
        cellFont.setBold(false);
        cellStyle.setFont(cellFont);

        return cellStyle;
    }

    private void createHeaderRow(XSSFSheet sheet, List<String> header, XSSFCellStyle headerStyle) {
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

    private void skipRows(Iterator<?> iterator, int skip) {
        for (int i = 0; i < skip; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            } else {
                break;
            }
        }
    }

    private void populateDataRows(XSSFSheet sheet, Iterator<T> iterator, XSSFCellStyle cellStyle, String pattern) {
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
                    log.error("Error while processing data row: {}", e.getMessage());
                    throw new RuntimeException("Error while processing data row", e);
                }
            }
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void setCellValue(XSSFCell cell, Object value, XSSFCellStyle cellStyle, String pattern) {
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

    private void setBorder(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
    }
}