package by.demon.zoom.service.impl;

import by.demon.zoom.dto.UrlDTO;
import by.demon.zoom.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class UrlService implements FileProcessingService<UrlDTO> {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);
    private static final List<String> HEADER = List.of("ID", "Ссылка конкурент");

    @Override
    public ArrayList<UrlDTO> readFiles(List<File> files, String... additionalParams) {
        ArrayList<UrlDTO> allUrlDTOs = new ArrayList<>(); // Создаем переменную для сохранения всех DTO

        for (File file : files) {
            try {
                List<List<Object>> excelData = readDataFromFile(file);
                Collection<UrlDTO> urlDTOList = getUrlDTOList(excelData);
                allUrlDTOs.addAll(urlDTOList); // Добавляем DTO из текущего файла в общую переменную
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
        return allUrlDTOs; // Возвращаем список всех DTO
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


