package by.demon.zoom.service.impl;

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

import static by.demon.zoom.mapper.MappingUtils.ifExistCompetitor;
import static by.demon.zoom.mapper.MappingUtils.listUsers;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static java.util.stream.Collectors.joining;

@Service
public class StatisticService implements FileProcessingService<List<Object>> {

    private static final Logger log = LoggerFactory.getLogger(StatisticService.class);
    private final DataDownload dataDownload;
    private final DataToExcel<List<Object>> dataToExcel;

    // Изменяемый список
    private static final List<String> header = new ArrayList<>(Arrays.asList("Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл."));

    public StatisticService(DataDownload dataDownload, DataToExcel<List<Object>> dataToExcel) {
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<List<Object>> readFiles(List<File> files, String... additionalParams) {

        List<Integer> columns = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 19, 20, 22, 23, 24);
        List<Integer> newColumn = getColumnList(additionalParams[0], additionalParams[2], additionalParams[3], columns);
        ArrayList<List<Object>> allListObj = new ArrayList<>();

        for (File file : files) {
            try {
                List<List<Object>> originalWb = readDataFromFile(file);
                List<List<Object>> resultTest = getResultList(originalWb, newColumn, additionalParams[1]);
                allListObj.addAll(resultTest);
                log.info("File {} successfully read", file.getName());
            } catch (IOException e) {
                log.error("Error reading data from file: {}", file.getAbsolutePath(), e);
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
        return allListObj;
    }

    public void download(ArrayList<List<Object>> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = DataDownload.getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        List<String> newHeader = addAdditionalColumnsToString(additionalParameters[2], additionalParameters[0], additionalParameters[3]);
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportObjectToExcel(newHeader, list, out, 1);
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


    private static List<Integer> getColumnList(String showSource, String showCompetitorUrl, String showDateAdd, List<Integer> source) {
        List<Integer> targetList = new ArrayList<>(source);
        addAdditionalColumns(targetList, showCompetitorUrl, showSource, showDateAdd);
        return targetList;
    }

    private static void addAdditionalColumns(List<Integer> columns, String showCompetitorUrl, String showSource, String showDateAdd) {
        if (showCompetitorUrl != null) {
            columns.add(27);
        }
        if (showSource != null) {
            columns.add(28);
        }
        if (showDateAdd != null) {
            columns.add(29);
        }
    }

    @Override
    public String save(ArrayList<List<Object>> collection) {
        return null;
    }

    private static List<String> addAdditionalColumnsToString(String showCompetitorUrl, String showSource, String showDateAdd) {
        List<String> updatedHeader = new ArrayList<>(header);
        if (showCompetitorUrl != null) {
            updatedHeader.add("URL");
        }
        if (showSource != null) {
            updatedHeader.add("Добавил");
        }
        if (showDateAdd != null) {
            updatedHeader.add("Добавил");
        }
        return updatedHeader;
    }


    private static List<List<Object>> getResultList(List<List<Object>> list, List<Integer> columnList, String sourceReplace) {
        List<List<Object>> newList = new LinkedList<>();
        for (List<Object> row : list) {
            List<Object> linked = new LinkedList<>();
            getRowList(linked, columnList, row, sourceReplace);
            newList.add(linked);
        }
        return newList;
    }

    private static void getRowList(List<Object> linked, List<Integer> columnList, List<Object> row, String sourceReplace) {
        for (int j = 0; j < row.size(); j++) {
            if (ifExistField(j, columnList)) {
                Object value = row.get(j);
                if (value == null) {
                    linked.add("");
                } else if (sourceReplace != null && j == 28 && !ifExistCompetitor((String) value, listUsers)) {
                    linked.add("manager");
                }  else {
                    linked.add(value);
                }
            }
        }
    }

    private static boolean ifExistField(int i, List<Integer> listColumns) {
        return listColumns.contains(i);
    }

}

