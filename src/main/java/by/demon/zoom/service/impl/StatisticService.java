package by.demon.zoom.service.impl;

import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static by.demon.zoom.mapper.MappingUtils.ifExistCompetitor;
import static by.demon.zoom.mapper.MappingUtils.listUsers;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class StatisticService implements FileProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticService.class);

    private static final List<String> HEADER = Arrays.asList("Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл.");

    private final DataToExcel<Object> dataToExcel;
    private final DataDownload dataDownload;

    public StatisticService(DataToExcel<Object> dataToExcel, DataDownload dataDownload) {
        this.dataToExcel = dataToExcel;
        this.dataDownload = dataDownload;
    }

    public String readFile(Path path, HttpServletResponse response, String... additionalParams) throws IOException {
        List<List<Object>> originalWb = readDataFromFile(path.toFile());
        List<Integer> columns = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 19, 20, 22, 23, 24);
        List<Integer> newColumn = getColumnList(additionalParams[0], additionalParams[2], additionalParams[3], columns);
        List<List<Object>> resultTest = getResultList(originalWb, newColumn, additionalParams[1]);
        try (OutputStream out = Files.newOutputStream(path)) {
            List<String> newHeader = addAdditionalColumnsToString(additionalParams[2], additionalParams[0], additionalParams[3]);
            short skip = 1;
            dataToExcel.exportToExcel(newHeader, resultTest, out, skip);
//            dataDownload.download(fileName, filePath, response);
        } catch (IOException e) {
            LOG.error("Error exporting data to Excel: {}", e.getMessage(), e);
            throw e;
        }
        LOG.info("Data exported successfully to Excel: {}", path.toAbsolutePath());
        return path.toAbsolutePath().toString();
    }

    @Override
    public void download(HttpServletResponse response,Path path,  String format, String... additionalParams) throws IOException {

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

    private static List<String> addAdditionalColumnsToString(String showCompetitorUrl, String showSource, String showDateAdd) {
        List<String> updatedHeader = new ArrayList<>(HEADER);
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

