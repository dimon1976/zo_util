package by.demon.zoom.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface FileProcessingService<T> {

    ArrayList<T> readFiles(List<File> files, String... additionalParams) throws IOException;

    String save(ArrayList<T> collection);
}