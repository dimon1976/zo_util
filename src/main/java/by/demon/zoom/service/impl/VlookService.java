package by.demon.zoom.service.impl;

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
public class VlookService implements FileProcessingService<VlookBarDTO> {

    private static final Logger log = LoggerFactory.getLogger(VlookService.class);
    private final DataToExcel<VlookBarDTO> dataToExcel;
    private final List<String> header = Arrays.asList("ID", "BAR", "URL");

    public VlookService( DataToExcel<VlookBarDTO> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<VlookBarDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        Map<String, Set<String>> mapOne = new HashMap<>();
        Map<String, Set<String>> mapTwo = new HashMap<>();
        ArrayList<VlookBarDTO> result = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<?>> futures = files.stream()
                .map(file -> executorService.submit(() -> {
                    try {
                        readDataFromFile(file)
                                .forEach(objects -> {
                                    try {
                                        addMapOne(objects, mapOne);
                                        addMapTwo(objects, mapTwo);
                                    } catch (Exception e) {
                                        log.error("Error processing data:", e);
                                    }
                                });

                        mapTwo.forEach((keyTwo, value) -> mapOne.getOrDefault(keyTwo, Collections.emptySet())
                                .forEach(keyOne -> value.forEach(url -> result.add(new VlookBarDTO(keyOne, keyTwo, url)))));

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

        return result;
    }

    public void download(ArrayList<VlookBarDTO> list, HttpServletResponse response, String format) throws IOException {
        Path path = getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        downloadFile(header, list, response, format, path, dataToExcel);
    }

    @Override
    public String save(ArrayList<VlookBarDTO> collection) {
        return null;
    }

    private void addMapOne(List<Object> list, Map<String, Set<String>> mapOne) {
        if (list.size() >= 2) {
            String id = String.valueOf(list.get(0));
            if (!id.isEmpty()) {
                String bar = String.valueOf(list.get(1));
                if (bar.length() > 13) {
                    String[] strings = bar.split(",");
                    Arrays.stream(strings)
                            .map(str -> str.replaceAll("\\D", ""))
                            .filter(barClear -> barClear.length() == 13)
                            .forEach(barClear -> mapOne.computeIfAbsent(barClear, k -> new HashSet<>()).add(id));
                } else {
                    lengthCheck(mapOne, id, bar);
                }
            }
        }
    }

    private void lengthCheck(Map<String, Set<String>> map, String key, String value) {
        if (value.length() == 13) {
            map.computeIfAbsent(value, k -> new HashSet<>()).add(key);
        }
    }

    private void addMapTwo(List<Object> list, Map<String, Set<String>> mapTwo) {
        if (list.size() > 3) {
            String url = String.valueOf(list.get(4));
            if (url.length() > 10) {
                String bar = String.valueOf(list.get(3));
                if (bar.length() > 13) {
                    String[] strings = bar.split(",");
                    Arrays.stream(strings)
                            .map(string -> string.replaceAll("\\D", ""))
                            .forEach(barClear -> lengthCheck(mapTwo, url, barClear));

                } else {
                    lengthCheck(mapTwo, url, bar);
                }
            }
        }
    }
}