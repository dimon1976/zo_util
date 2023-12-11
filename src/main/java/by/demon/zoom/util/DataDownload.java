package by.demon.zoom.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class DataDownload {

    private static final Logger LOG = LoggerFactory.getLogger(DataDownload.class);

    public void download(String filename, InputStream is, HttpServletResponse response) throws IOException {
        if (filename == null) {
            LOG.error("Filename is null. Unable to proceed with download.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename is null");
            return;
        }
        // Проверяем расширение файла и изменяем его, если необходимо на xlsx
        filename = filename.toLowerCase().endsWith(".xlsx") ? filename : filename.substring(0, filename.lastIndexOf('.')) + ".xlsx";

        LOG.info("Downloading file: {}", filename);
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes(), StandardCharsets.ISO_8859_1));
        response.setContentType("application/vnd.ms-excel;charset=gb2312");
        try (InputStream fis = new BufferedInputStream(is);
             OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                toClient.write(buffer, 0, bytesRead);
            }
            toClient.flush(); // Flush the output stream after writing all the data
        } catch (IOException ex) {
            LOG.error("Error during file download: {}", ex.getMessage());
            throw ex;
        }
    }


    public void download(String filename, String path, HttpServletResponse response) throws IOException {
        download(filename, new FileInputStream(path), response);
    }
}
