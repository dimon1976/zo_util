package by.demon.zoom.service.impl.lenta;

import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static java.util.stream.Collectors.joining;

@Service
public class EdadealService implements FileProcessingService<List<Object>> {

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

    @Override
    public ArrayList<List<Object>> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<List<Object>> allUrlDTOs = new ArrayList<>(); // Создаем переменную для сохранения всех DTO
        for (File file : files) {
            try {
                List<List<Object>> originalWb = readDataFromFile(file);
                Files.delete(file.toPath());
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

    public void download(ArrayList<List<Object>> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = DataDownload.getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportObjectToExcel(header, list, out, 1);
                        Files.write(path, out.toByteArray());
                    }
                    dataDownload.downloadExcel(path, response);
                    DataDownload.cleanupTempFile(path);
                    break;
                case "csv":
                    List<String> strings = convert(list);
                    //Пустой заголовок, т.к. лист уже содержит заголовок
                    List<String> fakeHeader = new ArrayList<>();
                    dataDownload.downloadCsv(path, strings, fakeHeader, response);
                    break;
                default:
                    log.error("Incorrect format: {}", format);
                    break;
            }

            log.info("Data exported successfully to {}: {}", format, path.getFileName().toString());
        } catch (IOException e) {
            log.error("Error exporting data to {}: {}", format, e.getMessage(), e);
            throw e;
        }
    }

    private static List<String> convert(List<List<Object>> objectList) {
        return objectList.stream()
                .map(row -> row.stream()
                        .map(String::valueOf)
                        .collect(joining(";")))
                .collect(Collectors.toList());
    }

    @Override
    public String save(ArrayList<List<Object>> collection) {
        return null;
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
