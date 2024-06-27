package by.demon.zoom.util;

import by.demon.zoom.dto.CsvRow;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.Globals.TEMP_PATH;

@Slf4j
@Component
public class FileUtil {

    private static final String CSV_CHARSET = "Windows-1251";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.ms-excel;charset=gb2312";
    private static final String CSV_CONTENT_TYPE = "text/csv;charset=Windows-1251";

    public static <T extends CsvRow> void downloadFile(List<String> header, List<T> list, HttpServletResponse response, String format, Path path, DataToExcel<T> dataToExcel) throws IOException {
        log.info("Starting file download. Format: {}, Path: {}", format, path);
        switch (format) {
            case "excel":
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    dataToExcel.exportToExcel(header, list, out, 0);
                    Files.write(path, out.toByteArray());
                    log.info("Excel file created successfully at {}", path);
                }
                downloadExcel(path, response);
                cleanupTempFile(path);
                log.info("Excel file downloaded and cleaned up.");
                break;
            case "csv":
                List<String> csvData = convertToCsv(list);
                downloadCsv(path, csvData, header, response);
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
                    dataToExcel.exportToExcel(header, list, out, 1);
                    Files.write(path, out.toByteArray());
                    log.info("Excel file created successfully at {}", path);
                }
                downloadExcel(path, response);
                cleanupTempFile(path);
                log.info("Excel file downloaded and cleaned up.");
                break;
            case "csv":
                List<String> csvData = list.stream()
                        .map(row -> row.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(";")))
                        .collect(Collectors.toList());
                downloadCsv(path, csvData, header, response);
                log.info("CSV file created and downloaded successfully at {}", path);
                break;
            default:
                log.error("Incorrect format: {}", format);
                throw new IllegalArgumentException("Incorrect format: " + format);
        }
    }

    public static void downloadExcel(Path path, HttpServletResponse response) throws IOException {
        if (path.getFileName() == null) {
            handleInvalidFilename(response);
            return;
        }

        log.info("Downloading file: {}", path.getFileName());
        long contentLength = Files.size(path);
        response.setContentLengthLong(contentLength);
        setResponseHeaders(response, path.getFileName().toString(), EXCEL_CONTENT_TYPE);

        try (InputStream fis = Files.newInputStream(path);
             OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {

            IOUtils.copy(fis, toClient);
        } catch (IOException ex) {
            handleDownloadError(ex);
        }
    }

    public static void downloadCsv(Path path, List<String> data, List<String> header, HttpServletResponse response) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStream outputStream = new BufferedOutputStream(baos)) {

            // Создать CSVWriter
            try (Writer writer = new OutputStreamWriter(outputStream, CSV_CHARSET);
                 CSVWriter csvWriter = new CSVWriter(writer, ';',
                         CSVWriter.DEFAULT_QUOTE_CHARACTER,
                         CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                         CSVWriter.DEFAULT_LINE_END)) {

                // Записать данные в CSV
                if (!header.isEmpty()) {
                    csvWriter.writeNext(header.toArray(new String[0]));
                }
                for (String line : data) {
                    csvWriter.writeNext(line.split(";"));
                }
            }

            // Установить заголовки ответа
            long contentLength = baos.size();
            response.setContentLengthLong(contentLength);
            setResponseHeaders(response, path.getFileName().toString(), CSV_CONTENT_TYPE);

            // Отправить данные в ответ
            baos.writeTo(response.getOutputStream());
        } catch (IOException e) {
            handleDownloadError(e);
        }
    }

    private static <T extends CsvRow> List<String> convertToCsv(List<T> list) {
        return list.stream()
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }

    private static void handleInvalidFilename(HttpServletResponse response) throws IOException {
        log.error("Filename is null");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
    }

    private static void setResponseHeaders(HttpServletResponse response, String filename, String contentType) {
        // Правильно закодировать имя файла для URL-совместимости
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setContentType(contentType);
    }

    private static void handleDownloadError(IOException ex) throws IOException {
        log.error("Error during file download: {}", ex.getMessage());
        throw ex;
    }

    public static Path getPath(String fileName, String suffix) {
        // Создаем временный файл и записываем в него данные
        String timestampLabel = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        return Path.of(TEMP_PATH, fileName + "-" + timestampLabel + suffix);
    }

    public static void cleanupTempFile(Path path) {
        if (path.toFile().delete()) {
            log.info("File removed successfully: {}", path.getFileName());
        } else {
            log.error("Failed to remove file: {}", path.getFileName());
        }
    }
}
