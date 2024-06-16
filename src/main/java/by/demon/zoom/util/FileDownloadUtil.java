package by.demon.zoom.util;

import by.demon.zoom.dto.CsvRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileDownloadUtil {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadUtil.class);

    public static <T extends CsvRow> void downloadFile(List<String> header, List<T> list, HttpServletResponse response, String format, Path path, DataToExcel<T> dataToExcel) throws IOException {
        log.info("Starting file download. Format: {}, Path: {}", format, path);
        switch (format) {
            case "excel":
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    dataToExcel.exportToExcel(header, list, out, 0);
                    Files.write(path, out.toByteArray());
                    log.info("Excel file created successfully at {}", path);
                }
                DataDownload.downloadExcel(path, response);
                DataDownload.cleanupTempFile(path);
                log.info("Excel file downloaded and cleaned up.");
                break;
            case "csv":
                List<String> csvData = convertToCsv(list);
                DataDownload.downloadCsv(path, csvData, header, response);
                log.info("CSV file created and downloaded successfully at {}", path);
                break;
            default:
                log.error("Incorrect format: {}", format);
                throw new IllegalArgumentException("Incorrect format: " + format);
        }
    }

    public static void downloadObjectFile(List<String> header, List<List<Object>> list, HttpServletResponse response, String format, Path path, DataToExcel<List<Object>> dataToExcel) throws IOException {
        log.info("Starting file download for objects. Format: {}, Path: {}", format, path);
        switch (format) {
            case "excel":
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    dataToExcel.exportObjectToExcel(header, list, out, 1);
                    Files.write(path, out.toByteArray());
                    log.info("Excel file created successfully at {}", path);
                }
                DataDownload.downloadExcel(path, response);
                DataDownload.cleanupTempFile(path);
                log.info("Excel file downloaded and cleaned up.");
                break;
            case "csv":
                List<String> csvData = list.stream()
                        .map(row -> row.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(";")))
                        .collect(Collectors.toList());
                DataDownload.downloadCsv(path, csvData, header, response);
                log.info("CSV file created and downloaded successfully at {}", path);
                break;
            default:
                log.error("Incorrect format: {}", format);
                throw new IllegalArgumentException("Incorrect format: " + format);
        }
    }

    private static <T extends CsvRow> List<String> convertToCsv(List<T> list) {
        return list.stream()
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }
}
