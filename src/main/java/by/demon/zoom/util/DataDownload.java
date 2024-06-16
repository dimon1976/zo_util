package by.demon.zoom.util;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static by.demon.zoom.util.Globals.TEMP_PATH;

@Slf4j
@Service
public class DataDownload {

    private static final Logger LOG = LoggerFactory.getLogger(DataDownload.class);

    public static void downloadExcel(Path path, HttpServletResponse response) throws IOException {
        if (path.getFileName() == null) {
            handleInvalidFilename(response);
            return;
        }

        LOG.info("Downloading file: {}", path.getFileName());
        long contentLength = Files.size(path);
        response.setContentLengthLong(contentLength);
        setResponseHeaders(response, path.getFileName().toString(), "application/vnd.ms-excel;charset=gb2312");

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
            try (Writer writer = new OutputStreamWriter(outputStream, "Windows-1251");
                 CSVWriter csvWriter = new CSVWriter(writer, ';',
                         CSVWriter.DEFAULT_QUOTE_CHARACTER,
                         CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                         CSVWriter.DEFAULT_LINE_END)) {

                // Записать данные в CSV
                if (!header.isEmpty()) {
                    String headersString = String.join(";", header);
                    List<String> headers = List.of(headersString);
                    writeCsvData(csvWriter, headers);
                }
                writeCsvData(csvWriter, data);
            }

            // Установить заголовки ответа
            long contentLength = baos.size();
            response.setContentLengthLong(contentLength);
            setResponseHeaders(response, path.getFileName().toString(), "text/csv;charset=Windows-1251");

            // Отправить данные в ответ
            baos.writeTo(response.getOutputStream());
        } catch (IOException e) {
            handleDownloadError(e);
        }
    }


    private static void handleInvalidFilename(HttpServletResponse response) throws IOException {
        LOG.error("Filename is null");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
    }

    private static void setResponseHeaders(HttpServletResponse response, String filename, String contentType) {
        // Правильно закодировать имя файла для URL-совместимости
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setContentType(contentType);
    }

    private static void writeCsvData(CSVWriter csvWriter, List<String> data) {
        for (String line : data) {
            String[] values = line.split(";");
            csvWriter.writeNext(values);
        }
    }

    private static void handleDownloadError(IOException ex) throws IOException {
        LOG.error("Error during file download: {}", ex.getMessage());
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
