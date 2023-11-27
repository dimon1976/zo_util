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
import java.util.List;

@Slf4j
@Service
public class DataDownload {

    private static final Logger LOG = LoggerFactory.getLogger(DataDownload.class);

    public void download(String filename, InputStream is, HttpServletResponse response) throws IOException {
        if (filename == null) {
            handleInvalidFilename(response);
            return;
        }

        filename = ensureXlsxExtension(filename);

        LOG.info("Downloading file: {}", filename);
        setResponseHeaders(response, filename, "application/vnd.ms-excel;charset=gb2312");

        try (InputStream fis = new BufferedInputStream(is);
             OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {

            copyStreamData(fis, toClient);
        } catch (IOException ex) {
            handleDownloadError(ex);
        }

    }

    public void download(List<String> data, File file, HttpServletResponse response, String parameter) throws IOException {
        try {
            setResponseHeaders(response, file.getName(), "text/csv;charset=Windows-1251");

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
        deleteFile(file);
    }


    public void download(String filename, String path, HttpServletResponse response) throws IOException {
        download(filename, new FileInputStream(path), response);
    }

    public void download(List<String> data, File file, HttpServletResponse response) throws IOException {
        download(data, file, response, "");
    }

    private void handleInvalidFilename(HttpServletResponse response) throws IOException {
        LOG.error("Filename is null");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
    }

    private String ensureXlsxExtension(String filename) {
        return filename.toLowerCase().endsWith(".xlsx") ? filename : filename.substring(0, filename.lastIndexOf('.')) + ".xlsx";
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

    private void deleteFile(File file) throws IOException {
        Files.deleteIfExists(file.toPath());
    }
}
