package by.demon.zoom.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;

public interface FileProcessingService {

    String readFile(Path path, HttpServletResponse response, String... additionalParams) throws IOException;

    void download(HttpServletResponse response, Path path, String format, String... additionalParams) throws IOException;
}
