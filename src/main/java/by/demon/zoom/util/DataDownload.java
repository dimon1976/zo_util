package by.demon.zoom.util;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    public void downloadExcel(Path path, InputStream is, HttpServletResponse response) throws IOException {
        if (path.getFileName() == null) {
            handleInvalidFilename(response);
            return;
        }

        LOG.info("Downloading file: {}", path.getFileName());
        setResponseHeaders(response, path.getFileName().toString(), "application/vnd.ms-excel;charset=gb2312");

        try (InputStream fis = new BufferedInputStream(is);
             OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {

            copyStreamData(fis, toClient);
        } catch (IOException ex) {
            handleDownloadError(ex);
        }

    }

    public void downloadCsv(Path path, List<String> data, HttpServletResponse response) throws IOException {
        try {
            setResponseHeaders(response, path.getFileName().toString(), "text/csv;charset=Windows-1251");

            try (OutputStream outputStream = response.getOutputStream();
                 Writer writer = new OutputStreamWriter(outputStream, "Windows-1251");
                 CSVWriter csvWriter = new CSVWriter(writer, ';',
                         CSVWriter.DEFAULT_QUOTE_CHARACTER,
                         CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                         CSVWriter.DEFAULT_LINE_END)) {

                writeCsvData(csvWriter, data);
                outputStream.flush();
            }
        } catch (IOException e) {
            handleDownloadError(e);
        }
        deleteFile(path);
    }


    private void handleInvalidFilename(HttpServletResponse response) throws IOException {
        LOG.error("Filename is null");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
    }

    private void setResponseHeaders(HttpServletResponse response, String filename, String contentType) {
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), StandardCharsets.ISO_8859_1));
        response.setContentType(contentType);
    }

    private void copyStreamData(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }

    private void writeCsvData(CSVWriter csvWriter, List<String> data) {
        for (String line : data) {
            String[] values = line.split(";");
            csvWriter.writeNext(values);
        }
    }

    private void handleDownloadError(IOException ex) throws IOException {
        LOG.error("Error during file download: {}", ex.getMessage());
        throw ex;
    }

    private void deleteFile(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    public static Path getPath(String fileName, String suffix) {
        // Создаем временный файл и записываем в него данные
        String timestampLabel = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
        return Path.of(TEMP_PATH, fileName + "-" + timestampLabel + suffix);
    }

    public static void cleanupTempFile(Path path) {
        if (path.toFile().delete()) {
            log.info("File removed successfully: {}", path.getFileName());
        } else {
            log.error("Failed to remove file: {}", path.getFileName());
        }
    }

    public static String setSuffix(String format) {
        if (format.equalsIgnoreCase("Excel")) {
            return Globals.SUFFIX_XLSX;
        } else if (format.equalsIgnoreCase("CSV")) {
            return Globals.SUFFIX_CSV;
        }
        LOG.error("Wrong format - {}", format);
        return "";
    }
}
