package by.demon.zoom.service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public interface FileProcessingService {

    String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException;

    String saveAll(String filePath, File transferTo, HttpServletResponse response, String... additionalParams) throws IOException;

    String deleteAll();
}
