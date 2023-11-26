package by.demon.zoom.service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public interface FileProcessingService {

    String readFile(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException;
}
