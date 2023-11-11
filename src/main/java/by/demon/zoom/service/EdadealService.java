package by.demon.zoom.service;

import by.demon.zoom.util.CsvUtil;
import by.demon.zoom.util.ExcelUtil;
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

import static by.demon.zoom.util.Globals.SUFFIX_XLSX;


@Service
public class EdadealService {
    private static final String EXCLUDE_STRING = "от ";
    private final List<String> header = Arrays.asList("Категория из файла", "Сайт", "ZMS ID", "Категория", "Бренд", "Модель", "Код производителя", "Цена", "Маркетинговое описание", "Маркетинговое описание 3",
            "Маркетинговое описание 4", "Статус", "Ссылка", "Старая цена", "Продавец", "Дата", "Позиция", "Ссылка на родителя");
    private final ExcelUtil<Object> excelUtil;

    public EdadealService(ExcelUtil<Object> excelUtil) {
        this.excelUtil = excelUtil;
    }

    public String export(String filePath, File file, HttpServletResponse response) throws IOException {
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
        return filePath;
    }


    private static List<List<Object>> getResultList(List<List<Object>> list, List<Integer> columnList) {
        List<List<Object>> newList = new LinkedList<>();
        int counter = 0;
        for (int i = 0; counter < list.size(); i++) {
            counter++;
            List<Object> linked = getRowList(columnList, list.get(i));
            if (linked != null) {
                newList.add(linked);
            }
        }
        return newList;
    }

    private static List<Object> getRowList(List<Integer> columnList, List<Object> row) {
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
                        return null;
                    } else {
                        linked.add(value);
                    }
                } else if (j == 24) {
                    // Проверка на пустые поля с моделью и продавцом
                    if (row.get(21).equals("") || row.get(11).equals("")) {
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
        return linked;
    }

    public static Boolean ifExistField(int i, List<Integer> listColumns) {
        return listColumns.stream()
                .anyMatch(numberColumns -> numberColumns == i);
    }
}
