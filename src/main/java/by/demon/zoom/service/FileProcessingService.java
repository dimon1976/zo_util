package by.demon.zoom.service;

import org.apache.poi.ss.formula.functions.T;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface FileProcessingService {

    //    String readFiles(Path path, HttpServletResponse response, String... additionalParams) throws IOException;
    Collection<T> readFiles(List<File> files, String... additionalParams) throws IOException;

    void save(Collection<T> collection);

    Collection<T> listAll();

    void download(HttpServletResponse response, Path path, String format, String... additionalParams) throws IOException;
}
