package by.demon.zoom.service.impl;

import by.demon.zoom.dto.imp.UrlDTO;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileUtil.downloadFile;
import static by.demon.zoom.util.FileUtil.getPath;

@Service
public class UrlService implements FileProcessingService<UrlDTO> {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);
    private final DataToExcel<UrlDTO> dataToExcel;
    private static final List<String> header = List.of("ID", "Ссылка конкурент");

    public UrlService(DataToExcel<UrlDTO> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<UrlDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<UrlDTO> allUrlDTOs = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<UrlDTO>>> futures = files.stream()
                .map(file -> executorService.<ArrayList<UrlDTO>>submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        List<List<Object>> excelData = readDataFromFile(file);
                        Collection<UrlDTO> urlDTOList = getUrlDTOList(excelData);
                        log.info("File {} successfully read", file.getName());
                        Files.delete(file.toPath());
                        return new ArrayList<>(urlDTOList);
                    } catch (Exception e) {
                        log.error("Failed to process file: {}", file.getName(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                        return new ArrayList<>();
                    }
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<UrlDTO>> future : futures) {
            try {
                allUrlDTOs.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }

        return allUrlDTOs;
    }

    public void download(ArrayList<UrlDTO> list, HttpServletResponse response, String format) throws IOException {
        Path path = getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        downloadFile(header, list, response, format, path, dataToExcel);
    }


    @Override
    public String save(ArrayList<UrlDTO> collection) {
        return null;
    }

    private Collection<UrlDTO> getUrlDTOList(List<List<Object>> excelData) {
        return excelData.stream()
                .flatMap(row -> row.stream()
                        .filter(cell -> cell instanceof String)
                        .map(cell -> (String) cell)
                        .filter(cell -> cell.startsWith("http://") || cell.startsWith("https://"))
                        .map(cell -> new UrlDTO(row.get(0).toString(), cell)))
                .collect(Collectors.toList());
    }
}
