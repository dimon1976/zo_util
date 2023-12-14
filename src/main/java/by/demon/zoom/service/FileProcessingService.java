package by.demon.zoom.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface FileProcessingService<T> {

    Collection<?> readFiles(List<File> files, String... additionalParams) throws IOException;
}