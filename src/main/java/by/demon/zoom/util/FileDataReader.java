package by.demon.zoom.util;

import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static by.demon.zoom.util.CsvReader.readCSV;
import static by.demon.zoom.util.ExcelReader.readExcel;

/**
 * @param <T>
 * @version 1.0
 */

@Slf4j
@Service
public class FileDataReader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FileDataReader.class);


    public static List<List<Object>> readDataFromFile(File file) {
        List<List<Object>> result = new ArrayList<>();

        try {
            if (file.getName().endsWith(".csv")) {
                result = readCSV(file);
            } else if (file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx")) {
                result = readExcel(file);
            } else {
                throw new UnsupportedOperationException("Unsupported file format");
            }
        } catch (IOException | CsvValidationException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return result;
    }
}
