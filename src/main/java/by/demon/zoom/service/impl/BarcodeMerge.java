package by.demon.zoom.service.impl;

import by.demon.zoom.dto.imp.BarcodeMergeDTO;
import by.demon.zoom.dto.imp.VlookBarDTO;
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
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileUtil.downloadFile;
import static by.demon.zoom.util.FileUtil.getPath;

@Service
public class BarcodeMerge implements FileProcessingService<BarcodeMergeDTO> {

    private static final Logger log = LoggerFactory.getLogger(BarcodeMerge.class);
    private final DataToExcel<BarcodeMergeDTO> dataToExcel;
    private final List<String> header = Arrays.asList("Model", "BAR");

    public BarcodeMerge( DataToExcel<BarcodeMergeDTO> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }


    @Override
    public ArrayList<BarcodeMergeDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        Map<String, Set<String>> mapOne = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();

        // Многопоточная обработка файлов
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<?>> futures = files.stream()
                .map(file -> executorService.submit(() -> {
                    try {
                        readDataFromFile(file)
                                .forEach(objects -> {
                                    try {
                                        addMapOne(objects, mapOne);
                                    } catch (Exception e) {
                                        log.error("Error processing data:", e);
                                    }
                                });
                        log.info("File {} successfully read", file.getName());
                        Files.delete(file.toPath());
                    } catch (Exception e) {
                        log.error("Failed to process file: {}", file.getName(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                    }
                }))
                .collect(Collectors.toList());

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }

        // Формирование результата
        ArrayList<BarcodeMergeDTO> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : mapOne.entrySet()) {
            String model = entry.getKey();
            String barcodes = String.join(",", entry.getValue());
            result.add(new BarcodeMergeDTO(model, barcodes));
        }

        return result;
    }

    public void download(ArrayList<BarcodeMergeDTO> list, HttpServletResponse response, String format) throws IOException {
        Path path = getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        downloadFile(header, list, response, format, path, dataToExcel);
    }

    private void addMapOne(List<Object> list, Map<String, Set<String>> mapOne) {
        if (list.size() >= 2) {
            String model = String.valueOf(list.get(0));
            if (!model.isEmpty()) {
                String bar = String.valueOf(list.get(1));
                Arrays.stream(bar.split(","))
                        .map(str -> str.replaceAll("\\D", "")) // Убираем нецифровые символы
                        .filter(barcode -> barcode.length() == 13) // Проверяем длину штрихкода
                        .forEach(barcode -> mapOne.computeIfAbsent(model, k -> new HashSet<>()).add(barcode));
            }
        }
    }

    @Override
    public String save(ArrayList<BarcodeMergeDTO> collection) {
        return null;
    }
}
