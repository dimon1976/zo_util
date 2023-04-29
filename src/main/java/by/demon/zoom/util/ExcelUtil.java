package by.demon.zoom.util;

import by.demon.zoom.domain.VlookBar;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @param <T>
 * @version 1.0
 */
@Service
public class ExcelUtil<T> {

    private static final DecimalFormat df = new DecimalFormat("0");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat nf = new DecimalFormat("0.00");


    public static List<List<Object>> readExcel(File file) throws IOException {
        String fileName = file.getName();
        String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1);
        if ("xls".equals(extension)) {
            return readExcel2003(file);
        } else if ("xlsx".equals(extension)) {
            return readExcel2007(file);
        } else {
            throw new IOException("Неподдерживаемый тип файлов");
        }
    }


    public static List<List<Object>> readExcel(InputStream is, String suffix) throws IOException {
        if (Globals.SUFFIX_XLS.equals(suffix)) {
            return readExcel2003(is);
        } else if (Globals.SUFFIX_XLSX.equals(suffix)) {
            return readExcel2007(is);
        } else {
            throw new IOException("Неподдерживаемый тип файлов");
        }
    }


    public void download(String filename, InputStream is, HttpServletResponse response) throws IOException {
        InputStream fis = new BufferedInputStream(is);
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        response.reset();
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), StandardCharsets.ISO_8859_1));
        OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/vnd.ms-excel;charset=gb2312");
        toClient.write(buffer);
        toClient.flush();
        toClient.close();
        is.close();
    }


    public void download(String filename, String path, HttpServletResponse response) throws IOException {
        download(filename, new FileInputStream(path), response);
    }


    public static String export2003(String fileName, HttpServletResponse response) throws IOException {
        return null;
    }

    public static String export2007(String fileName, HttpServletResponse response) throws IOException {
        return null;
    }

    private static List<List<Object>> readExcel2003(InputStream is) throws IOException {
        List<List<Object>> list = new LinkedList<>();
        HSSFWorkbook hwb = new HSSFWorkbook(is);
        HSSFSheet sheet = hwb.getSheetAt(0);
        Object value;
        HSSFRow row;
        HSSFCell cell;
        int counter = 0;
        for (int i = sheet.getFirstRowNum(); counter < sheet.getPhysicalNumberOfRows(); i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            } else {
                counter++;
            }
            List<Object> linked = new LinkedList<>();
            for (int j = 0; j <= row.getLastCellNum(); j++) {
                cell = row.getCell(j);
                if (cell == null) {
                    linked.add(null);
                    continue;
                }
                switch (cell.getCellType()) {
                    case STRING:
                        value = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                            value = df.format(cell.getNumericCellValue());
                        } else if ("General".equals(
                                cell.getCellStyle().getDataFormatString())) {
                            value = nf.format(cell.getNumericCellValue());
                        } else {
                            value = sdf.format(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue()));
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
                if (value == null || "".equals(value)) {
                    continue;
                }
                linked.add(value);
            }
            list.add(linked);
        }
        return list;
    }

    private static List<List<Object>> readExcel2003(File file) throws IOException {
        return readExcel2003(new FileInputStream(file));
    }

    private static List<List<Object>> readExcel2007(InputStream is) throws IOException {
        List<List<Object>> list = new LinkedList<>();
        XSSFWorkbook xwb = new XSSFWorkbook(is);
        XSSFSheet sheet = xwb.getSheetAt(0);
        Object value;
        XSSFRow row;
        XSSFCell cell;
        int counter = 0;
        for (int i = sheet.getFirstRowNum(); counter < sheet.getPhysicalNumberOfRows(); i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            } else {
                counter++;
            }
            List<Object> linked = new LinkedList<>();
            for (int j = 0; j <= row.getLastCellNum(); j++) {
                cell = row.getCell(j);
                if (cell == null) {
                    linked.add("");
                    continue;
                }
//                switch (cell.getCellType()) {
//                    case STRING:
//                        value = cell.getStringCellValue();
//                        break;
//                    case NUMERIC:
//                        String dataFormatString = cell.getCellStyle().getDataFormatString();
//                        if (cell.getCellType() == CellType.FORMULA) {
//                            value = getValueFormula(cell, sdf);
//                            linked.add(value);
//                        } else {
//                            if ("@".equals(cell.getCellStyle().getDataFormatString())) {
//                                value = df.format(cell.getNumericCellValue());
//                            } else if ("General".equals(cell.getCellStyle().getDataFormatString())) {
//                                value = nf.format(cell.getNumericCellValue());
//                            } else {
//                                value = sdf.format(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue()));
//                            }
//                        }
//                        break;
//                    case BOOLEAN:
//                        value = cell.getBooleanCellValue();
//                        break;
//                    case BLANK:
//                        value = "";
//                        break;
//                    default:
//                        value = cell.toString();
//                }
                if (cell.getCellType() == CellType.FORMULA) {
                    value = getValueFormula(cell);
                    linked.add(value);
                } else {
                    value = getValue(cell);
                    linked.add(value);
                }
//                linked.add(value);
            }
            list.add(linked);
        }
        is.close();
        return list;
    }

    private static Object getValueFormula(XSSFCell cell) {
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
                break;
            default:
                value = cell.toString();
        }
        return value;
    }

    private static Object getValue(XSSFCell cell) {
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

    public void exportExcel(Collection<T> dataset, OutputStream out, short skip) {
        exportExcel(Globals.SHEETNAME, null, dataset, out, "yyyy-MM-dd", skip);
    }

    public void exportExcel(String[] headers, Collection<T> dataset, OutputStream out, short skip) {
        exportExcel(Globals.SHEETNAME, headers, dataset, out, "yyyy-MM-dd", skip);
    }

    public void exportExcel(String[] headers, Collection<T> dataset, OutputStream out, String pattern, short skip) {
        exportExcel(Globals.SHEETNAME, headers, dataset, out, pattern, skip);
    }

    /**
     * @param title   Title
     * @param headers Заголовок
     * @param dataset Коллекция
     * @param out     Выходной поток
     * @param pattern Паттерн для замены
     */
    public void exportExcel(String title, String[] headers, Collection<T> dataset, OutputStream out, String pattern, short skip) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(title);
            sheet.setDefaultColumnWidth((short) 15);
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
//            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont cellFont = workbook.createFont();
            cellFont.setBold(false);
            cellStyle.setFont(cellFont);
            XSSFRow row = sheet.createRow(0);
            if (headers != null) {
                for (short i = 0; i < headers.length; i++) {
                    XSSFCell cell = row.createCell(i);
                    cell.setCellStyle(headerStyle);
                    XSSFRichTextString text = new XSSFRichTextString(headers[i]);
                    cell.setCellValue(text);
                }
            }
            Iterator<T> it = dataset.iterator();
            int index = 0;
            short marker = 0;
            while (it.hasNext()) {
                if (marker < skip) {
                    it.next();
                    marker++;
                    continue;
                }
                index++;
                row = sheet.createRow(index);
                T t = it.next();
                Field[] fields = t.getClass().getDeclaredFields();
                for (short i = 0; i < fields.length; i++) {
                    XSSFCell cell = row.createCell(i);
                    cell.setCellStyle(cellStyle);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    try {
                        Class<?> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName);
                        Object value = getMethod.invoke(t);
                        String textValue = null;
                        if (null == value) {
                            value = "";
                        }
                        if (value instanceof Boolean) {
                            boolean bValue = (Boolean) value;
                            textValue = "true";
                            if (!bValue) {
                                textValue = "false";
                            }
                        } else if (value instanceof Date) {
                            Date date = (Date) value;
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                            textValue = sdf.format(date);
                        } else if (value instanceof byte[]) {
                            row.setHeightInPoints(60);
                            sheet.setColumnWidth(i, (short) (35.7 * 80));
                            byte[] bsValue = (byte[]) value;
                        } else {
                            textValue = value.toString();
                        }
                        if (textValue != null) {
                            Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                            Matcher matcher = p.matcher(textValue);
                            if (matcher.matches()) {
                                cell.setCellValue(Double.parseDouble(textValue));
                            } else {
                                XSSFRichTextString richString = new XSSFRichTextString(textValue);
                                XSSFFont font3 = workbook.createFont();
                                richString.applyFont(font3);
                                cell.setCellValue(richString);
                            }
                        }
                    } catch (SecurityException | InvocationTargetException | IllegalAccessException |
                             IllegalArgumentException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                workbook.write(out);
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void export2003(String imagesPath, String docsPath) {
//        ExcelExportUtil<Student> ex = new ExcelExportUtil<Student>();
//        String[] headers = {"学号", "姓名", "年龄", "性别", "出生日期"};
//        List<Student> dataset = new ArrayList<Student>();
//        dataset.add(new Student(10000001L, "张三", 20, true, new Date()));
//        dataset.add(new Student(20000002L, "李四", 24, false, new Date()));
//        dataset.add(new Student(30000003L, "王五", 22, true, new Date()));
//        ExcelExportUtil<Book> exBook = new ExcelExportUtil<Book>();
//        String[] headersBook = {"图书编号", "图书名称", "图书作者", "图书价格", "图书ISBN", "图书出版社", "封面图片"};
//        List<Book> datasetBook = new ArrayList<Book>();
//        try {
//            Resource resource = new ClassPathResource(imagesPath);
//            InputStream is = resource.getInputStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//            byte[] buf = new byte[bis.available()];
//            while ((bis.read(buf)) != -1) {
//                //
//            }
//            datasetBook.add(new Book(1, "jsp", "leno", 300.33f, "1234567",
//                    "清华出版社", buf));
//            datasetBook.add(new Book(2, "java编程思想", "brucl", 300.33f, "1234567",
//                    "阳光出版社", buf));
//            datasetBook.add(new Book(3, "DOM艺术", "lenotang", 300.33f, "1234567",
//                    "清华出版社", buf));
//            datasetBook.add(new Book(4, "c++经典", "leno", 400.33f, "1234567",
//                    "清华出版社", buf));
//            datasetBook.add(new Book(5, "c#入门", "leno", 300.33f, "1234567",
//                    "汤春秀出版社", buf));
//            OutputStream out = new FileOutputStream(
//                    docsPath + File.separator + Globals.EXPORT_STUDENT);
//            OutputStream outBook = new FileOutputStream(
//                    docsPath + File.separator + Globals.EXPORT_BOOK);
//            ex.exportExcel(headers, dataset, out);
//            exBook.exportExcel(headersBook, datasetBook, outBook);
//            out.close();
//            outBook.close();

//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void export2007(String filePath) {
        try {
            OutputStream os = Files.newOutputStream(Paths.get(filePath));
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet(Globals.SHEETNAME);
            for (int i = 0; i < 1000; i++) {
                XSSFRow row = sheet.createRow(i);
                row.createCell(0).setCellValue("column" + i);
                row.createCell(1).setCellValue("column" + i);
            }
            wb.write(os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
