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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileUtil.downloadObjectFile;
import static by.demon.zoom.util.FileUtil.getPath;

@Service
public class YandexUrlService implements FileProcessingService<List<Object>> {

    private static final Logger log = LoggerFactory.getLogger(YandexUrlService.class);
    private static final List<String> header = new ArrayList<>(Arrays.asList("UUID", "Модель", "Ссылка"));
    private final DataToExcel<List<Object>> dataToExcel;

    public YandexUrlService(DataToExcel<List<Object>> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }


    @Override
    public ArrayList<List<Object>> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<List<Object>> allListObj = new ArrayList<>();
        List<String> errorMessages = Collections.synchronizedList(new ArrayList<>());

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<List<Object>>>> futures = files.stream()
                .map(file -> executorService.submit(() -> processFile(file, errorMessages)))
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

    private ArrayList<List<Object>> processFile(File file, List<String> errorMessages) {
        try {
            List<List<Object>> originalData = readDataFromFile(file);
            List<List<Object>> processedData = processData(originalData);
            log.info("File {} successfully read", file.getName());
            Files.delete(file.toPath());
            return new ArrayList<>(processedData);
        } catch (Exception e) {
            log.error("Failed to process file: {}", file.getName(), e);
            errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
            return new ArrayList<>();
        }
    }
    private ArrayList<List<Object>> processData(List<List<Object>> data) {
        ArrayList<List<Object>> processedData = new ArrayList<>();
        for (List<Object> row : data) {
            List<Object> newRow = new ArrayList<>();
            newRow.add(UUID.randomUUID().toString());
            newRow.add(row.get(0)); // Модель
            newRow.add(cleanUrl((String) row.get(1))); // Очищенная ссылка
            processedData.add(newRow);
        }
        return processedData;
    }

    private String cleanUrl(String url) {
        String skuValue = findSkuParameter(url);
        if (skuValue != null) {
            return url.split("\\?")[0] + "?" + skuValue;
        } else {
            return url.split("\\?")[0];
        }
    }

    public static String findSkuParameter(String text) {
        // Регулярное выражение для поиска 'sku=' с последующим значением
        String regex = "(sku=[^&]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1); // Возвращаем параметр SKU (включая 'sku=')
        }
        return null; // Возвращаем null, если SKU не найден
    }


    public void download(ArrayList<List<Object>> list, HttpServletResponse response, String format) throws IOException {
        Path path = getPath("clear-yandex-urls", format.equals("excel") ? ".xlsx" : ".csv");
        downloadObjectFile(header, list, response, format, path, dataToExcel);
    }

    @Override
    public String save(ArrayList<List<Object>> collection) {
        return null;
    }
}
