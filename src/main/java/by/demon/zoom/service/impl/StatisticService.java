package by.demon.zoom.service.impl;

import by.demon.zoom.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static by.demon.zoom.mapper.MappingUtils.ifExistCompetitor;
import static by.demon.zoom.mapper.MappingUtils.listUsers;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class StatisticService implements FileProcessingService<List<Object>> {

    private static final Logger log = LoggerFactory.getLogger(StatisticService.class);

    // Изменяемый список
    private static final List<String> header = new ArrayList<>(Arrays.asList("Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл."));

    @Override
    public Collection<List<Object>> readFiles(List<File> files, String... additionalParams) {
        ArrayList<List<Object>> allListObj = new ArrayList<>();

        List<Integer> columns = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 19, 20, 22, 23, 24);
        List<Integer> newColumn = getColumnList(additionalParams[0], additionalParams[2], additionalParams[3], columns);
        addAdditionalColumnsToString(additionalParams[2], additionalParams[0], additionalParams[3]);

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
    public String save(Collection<List<Object>> collection) {
        return null;
    }

    private static void addAdditionalColumnsToString(String showCompetitorUrl, String showSource, String showDateAdd) {
//        List<String> updatedHeader = new ArrayList<>(header);
        if (showCompetitorUrl != null) {
            header.add("URL");
        }
        if (showSource != null) {
            header.add("Добавил");
        }
        if (showDateAdd != null) {
            header.add("Добавил");
        }
//        return updatedHeader;
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

    private static void getRowList(List<Object> linked, List<Integer> columnList, List<Object> row, String
            sourceReplace) {
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
    }

    private static boolean ifExistField(int i, List<Integer> listColumns) {
        return listColumns.contains(i);
    }

}

