package by.demon.zoom.service.impl;

import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.dto.imp.UrlDTO;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class UrlService implements FileProcessingService<UrlDTO> {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);
    private final DataDownload dataDownload;
    private final DataToExcel<UrlDTO> dataToExcel;
    private static final List<String> header = List.of("ID", "Ссылка конкурент");

    public UrlService(DataDownload dataDownload, DataToExcel<UrlDTO> dataToExcel) {
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
    }

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

    public void download(ArrayList<UrlDTO> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = DataDownload.getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportToExcel(header, list, out, 0);
                        Files.write(path, out.toByteArray());
                    }
                    dataDownload.downloadExcel(path, response);
                    DataDownload.cleanupTempFile(path);
                    break;
                case "csv":
                    List<String> strings = convert(list);
                    dataDownload.downloadCsv(path, strings, header, response);
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

    private static List<String> convert(List<UrlDTO> objectList) {
        return objectList.stream()
                .filter(Objects::nonNull)
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
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


