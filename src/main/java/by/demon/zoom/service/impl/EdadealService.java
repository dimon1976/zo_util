package by.demon.zoom.service.impl;

import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class EdadealService implements FileProcessingService<T> {

    private static final Logger log = LoggerFactory.getLogger(EdadealService.class);
    private static final String EXCLUDE_STRING = "от ";
    private final List<String> header = Arrays.asList("Категория из файла", "Сайт", "ZMS ID", "Категория", "Бренд", "Модель", "Код производителя", "Цена", "Маркетинговое описание", "Маркетинговое описание 3",
            "Маркетинговое описание 4", "Статус", "Ссылка", "Старая цена", "Продавец", "Дата", "Позиция", "Ссылка на родителя");

    private final DataToExcel<Object> dataToExcel;
    private final DataDownload dataDownload;

    public EdadealService(DataToExcel<Object> dataToExcel, DataDownload dataDownload) {
        this.dataToExcel = dataToExcel;
        this.dataDownload = dataDownload;
    }

//    public String readFiles(Path path, HttpServletResponse response, String... additionalParams) throws IOException {
//        log.info("Exporting data...");
//        List<List<Object>> originalWb = readDataFromFile(path.toFile());
//        List<Integer> columns = Arrays.asList(0, 1, 2, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24);
//        List<List<Object>> resultList = getResultList(originalWb, columns);
//        try (OutputStream out = Files.newOutputStream(path);
//             FileInputStream is = new FileInputStream(path.toAbsolutePath().toString())) {
//            short skip = 1;
//            dataToExcel.exportToExcel(header, resultList, out, skip);
//            dataDownload.downloadExcel(path, is, response);
//        }
//        log.info("Exported data to Excel: {}", path.toAbsolutePath());
//        return path.toAbsolutePath().toString();
//    }


    @Override
    public Collection<List<Object>> readFiles(List<File> files, String... additionalParams) throws IOException {
        Collection<List<Object>> allUrlDTOs = new ArrayList<>(); // Создаем переменную для сохранения всех DTO
        for (File file : files) {
            try {
                List<List<Object>> originalWb = readDataFromFile(file);
                List<Integer> columns = Arrays.asList(0, 1, 2, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24);
                List<List<Object>> resultList = getResultList(originalWb, columns);
                allUrlDTOs.addAll(resultList);
            } catch (Exception e) {
                log.error("Error processing file: {}", file.getAbsolutePath(), e);

            } finally {
                if (file.exists()) {
                    if (!file.delete()) {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        return allUrlDTOs;
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
