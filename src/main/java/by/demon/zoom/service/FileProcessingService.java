package by.demon.zoom.service;

import org.apache.poi.ss.formula.functions.T;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface FileProcessingService {

    String readFile(Path path, HttpServletResponse response, String... additionalParams) throws IOException;

    void download(HttpServletResponse response, Path path, String format, String... additionalParams) throws IOException;

    void save(Collection<T> collection);

    Collection<T> getData();
}
