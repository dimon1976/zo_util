package by.demon.zoom.service.impl;

import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.CsvUtil;
import by.demon.zoom.util.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
public class EdadealService implements FileProcessingService {

    private static final String EXCLUDE_STRING = "от ";
    private static final String SUFFIX_XLSX = ".xlsx";

    private final List<String> header = Arrays.asList("Категория из файла", "Сайт", "ZMS ID", "Категория", "Бренд", "Модель", "Код производителя", "Цена", "Маркетинговое описание", "Маркетинговое описание 3",
            "Маркетинговое описание 4", "Статус", "Ссылка", "Старая цена", "Продавец", "Дата", "Позиция", "Ссылка на родителя");

    private final ExcelUtil<Object> excelUtil;

    private static final Logger log = LoggerFactory.getLogger(EdadealService.class);

    public EdadealService(ExcelUtil<Object> excelUtil) {
        this.excelUtil = excelUtil;
    }

    @Override
    public String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        log.info("Exporting data...");
        String fileName = file.getName();
        String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1);
        List<List<Object>> originalWb;
        if ("csv".equals(extension)) {
            originalWb = CsvUtil.readFile(file);
            fileName = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(0, fileName.lastIndexOf(".")) + SUFFIX_XLSX;
        } else {
            originalWb = ExcelUtil.readExcel(file);
        }
        List<Integer> columns = Arrays.asList(0, 1, 2, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24);
        List<List<Object>> resultList = getResultList(originalWb, columns);
        try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
            short skip = 1;
            excelUtil.exportExcel(header, resultList, out, skip);
            excelUtil.download(fileName, filePath, response);
        }
        log.info("Exported data to Excel: {}", filePath);
        return filePath;
    }

    private List<List<Object>> getResultList(List<List<Object>> list, List<Integer> columnList) {
        log.debug("Getting result list...");
        List<List<Object>> newList = new LinkedList<>();
        int counter = 0;
        for (int i = 0; counter < list.size(); i++) {
            counter++;
            List<Object> linked = getRowList(columnList, list.get(i));
            if (linked != null) {
                newList.add(linked);
            }
        }
        log.debug("Result list size: {}", newList.size());
        return newList;
    }

    private List<Object> getRowList(List<Integer> columnList, List<Object> row) {
        log.debug("Getting row list...");
        Object value;
        List<Object> linked = new LinkedList<>();
        for (int j = 0; j <= row.size(); j++) {
            if (ifExistField(j, columnList)) {
                value = row.get(j);
                if (value == null) {
                    linked.add("");
                    break;
                } else if (j == 16) {
                    // Проверка на вхождение подстроки "от" для исключения строки из выгрузки
                    if (value.toString().startsWith(EXCLUDE_STRING)) {
                        log.debug("Excluded row: {}", row);
                        return null;
                    } else {
                        linked.add(value);
                    }
                } else if (j == 24) {
                    // Проверка на пустые поля с моделью и продавцом
                    if (row.get(21).equals("") || row.get(11).equals("")) {
                        log.debug("Excluded row due to empty model or seller: {}", row);
                        return null;
                    } else {
                        value = row.get(21) + "-" + row.get(11);
                        linked.add(value);
                    }
                } else {
                    linked.add(value);
                }
            }
        }
        log.debug("Row list size: {}", linked.size());
        return linked;
    }

    public static Boolean ifExistField(int i, List<Integer> listColumns) {
        log.debug("Checking if column {} exists in the list...", i);
        return listColumns.contains(i);
    }
}
