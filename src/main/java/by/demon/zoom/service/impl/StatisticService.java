package by.demon.zoom.service.impl;

import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.mapper.MappingUtils.ifExistCompetitor;
import static by.demon.zoom.mapper.MappingUtils.listUsers;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileUtil.downloadObjectFile;
import static by.demon.zoom.util.FileUtil.getPath;

@Service
public class StatisticService implements FileProcessingService<List<Object>> {

    private static final Logger log = LoggerFactory.getLogger(StatisticService.class);
    private final DataToExcel<List<Object>> dataToExcel;

    private static final List<String> header = new ArrayList<>(Arrays.asList("Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл."));

    public StatisticService(DataToExcel<List<Object>> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<List<Object>> readFiles(List<File> files, String... additionalParams) throws IOException {
        List<Integer> columns = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 19, 20, 22, 23, 24);
        List<Integer> newColumn = getColumnList(additionalParams[0], additionalParams[2], additionalParams[3], columns);
        ArrayList<List<Object>> allListObj = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<List<Object>>>> futures = files.stream()
                .map(file -> executorService.<ArrayList<List<Object>>>submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        List<List<Object>> originalWb = readDataFromFile(file);
                        List<List<Object>> resultTest = getResultList(originalWb, newColumn, additionalParams[1]);
                        log.info("File {} successfully read", file.getName());
                        Files.delete(file.toPath());
                        return new ArrayList<>(resultTest);
                    } catch (Exception e) {
                        log.error("Failed to process file: {}", file.getName(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                        return new ArrayList<>();
                    }
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<List<Object>>> future : futures) {
            try {
                allListObj.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }

        return allListObj;
    }

    public void download(ArrayList<List<Object>> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        List<String> newHeader = addAdditionalColumnsToString(additionalParameters[2], additionalParameters[0], additionalParameters[3]);
        Path path = getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        downloadObjectFile(newHeader, list, response, format, path, dataToExcel);
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
        return list.stream()
                .map(row -> getRowList(new LinkedList<>(), columnList, row, sourceReplace))
                .collect(Collectors.toList());
    }

    private static List<Object> getRowList(List<Object> linked, List<Integer> columnList, List<Object> row, String sourceReplace) {
        for (int j = 0; j < row.size(); j++) {
            if (ifExistField(j, columnList)) {
                Object value = row.get(j);
                if (value == null) {
                    linked.add("");
                } else if (sourceReplace != null && j == 28 && !ifExistCompetitor((String) value, listUsers)) {
                    linked.add("manager");
                } else {
                    linked.add(value);
                }
            }
        }
        return linked;
    }

    private static boolean ifExistField(int i, List<Integer> listColumns) {
        return listColumns.contains(i);
    }
}
