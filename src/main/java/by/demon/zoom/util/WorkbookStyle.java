package by.demon.zoom.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;


@Service
public class WorkbookStyle {

    private static final String DEFAULT_FONT_NAME = "Calibri";


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

    public static void setBorder(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
    }
}
