package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final short DEFAULT_COLUMN_WIDTH = 15;
    private static final String DEFAULT_FONT_NAME = "Calibri";


    public static List<List<Object>> readExcel(File file) throws IOException {
        String fileName = file.getName();
        String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1);
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
            log.error("Filename is null. Unable to proceed with download.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
            return;
        }
        log.info("Downloading file: {}", filename);
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(StandardCharsets.UTF_8)));
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        try (InputStream fis = new BufferedInputStream(is);
             OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                toClient.write(buffer, 0, bytesRead);
            }
            toClient.flush(); // Flush the output stream after writing all the data
        } catch (IOException ex) {
            log.error("Error during file download: {}", ex.getMessage());
            throw ex;
        }
    }


    public void download(String filename, String path, HttpServletResponse response) throws IOException {
        download(filename, new FileInputStream(path), response);
    }


    private static List<List<Object>> readExcel2007(InputStream is) throws IOException {
        List<List<Object>> list = new LinkedList<>();
        // https://stackoverflow.com/a/75830526
        IOUtils.setByteArrayMaxOverride(1000000000);
        XSSFWorkbook xwb = new XSSFWorkbook(is);
        XSSFSheet sheet = xwb.getSheetAt(0);
        XSSFRow row;
        int counter = 0;
        for (int i = sheet.getFirstRowNum(); counter < sheet.getPhysicalNumberOfRows(); i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            } else {
                counter++;
            }
            List<Object> linked = new LinkedList<>();
            getRowList(row, linked);
            list.add(linked);
        }
        is.close();
        return list;
    }

    public static void getRowList(Row row, List<Object> linked) {
        Cell cell;
        Object value;
        for (int j = 0; j <= 250; j++) {
            cell = row.getCell(j);
            if (cell == null) {
                linked.add("");
                continue;
            }
            if (cell.getCellType() == CellType.FORMULA) {
                value = getValueFormula(cell);
                linked.add(value);
            } else {
                value = getValue(cell);
                linked.add(value);
            }
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
                    value = ExcelUtil.sdf.format(DateUtil.getJavaDate(cell.getNumericCellValue()));
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
//                value = Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
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
                    value = ExcelUtil.sdf.format(DateUtil.getJavaDate(cell.getNumericCellValue()));
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

    public void exportExcel(List<String> headers, Collection<T> dataset, OutputStream out, short skip) {
        exportExcel(Globals.SHEET_NAME, headers, dataset, out, "yyyy-MM-dd", skip);
    }

    public void exportExcel(List<String> headers, List<List<Object>> dataset, OutputStream out, short skip) {
        exportObjectToExcel(Globals.SHEET_NAME, headers, dataset, out, "yyyy-MM-dd", skip);
    }

    /**
     * @param title   Title
     * @param headers Заголовок
     * @param dataset Коллекция
     * @param out     Выходной поток
     * @param pattern Паттерн для замены
     */

    public void exportExcel(String title, List<String> headers, Collection<T> dataset, OutputStream out, String pattern, short skip) {
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
            log.error("Error while exporting Excel: {}", e.getMessage());
            throw new RuntimeException("Error while exporting Excel", e);
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

    private void createHeaderRow(XSSFSheet sheet, List<String> headers, XSSFCellStyle headerStyle) {
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(headers.get(i));
        }
    }

    private void skipRows(Iterator<T> iterator, short skip) {
        for (int i = 0; i < skip; i++) {
            if (iterator.hasNext()) {
                iterator.next();
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
                    setCellValue(cell, value, pattern);
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

    private void setCellValue(XSSFCell cell, Object value, String pattern) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if (value instanceof Boolean) {

            cell.setCellValue((Boolean) value);
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
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private void setBorder(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
    }


    public void exportObjectToExcel(String title, List<String> headers, List<List<Object>> dataset, OutputStream out, String pattern, short skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(title);
            sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle cellStyle = createCellStyle(workbook);

            createHeaderRow(sheet, headers, headerStyle);

            int rowNum = 0;
            int skipCount = 0;
            for (List<Object> rowData : dataset) {
                if (skipCount < skip) {
                    skipCount++;
                    continue;
                }

                XSSFRow row = sheet.createRow(rowNum++);
                int colNum = 0;
                for (Object value : rowData) {
                    XSSFCell cell = row.createCell(colNum++);
                    setCellValue(cell, value, pattern);
                }
            }
            workbook.write(out);
        } catch (IOException e) {
            log.error("Error exporting object to Excel: {}", e.getMessage());
            throw new RuntimeException("Error exporting object to Excel", e);
        }
    }

}