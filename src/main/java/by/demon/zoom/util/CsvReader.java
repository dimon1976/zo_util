package by.demon.zoom.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.Globals.TEMP_PATH;


@Service
public class CsvReader {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReader.class);
    private static int max = 0;


    public static List<List<Object>> readCSV(File file) throws IOException, CsvValidationException {
        LOG.info(String.format("Processing file: %s", file.getName()));
        Path path = getPath(file.getName());
        String charset = getCharset(path.toFile());
        String separator = findSeparator(getFirstRowsInFile(file, charset));
        CSVParser parser = new CSVParserBuilder().withSeparator(separator.charAt(0)).build();
        List<List<Object>> csvData = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(path, Charset.forName(charset));
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(0).withCSVParser(parser).build()) {

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                List<Object> row = new ArrayList<>(Arrays.asList(nextLine));
                csvData.add(row);
            }
        } catch (IOException | CsvValidationException e) {
            LOG.error("Error reading CSV file: {}", e.getMessage());
            throw e;  // Propagate the exception to the calling code
        }
        return csvData;
    }

    private static String getCharset(File file) throws IOException {
        try (InputStream fis = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
            UniversalDetector detector = new UniversalDetector();
            byte[] buf = new byte[4096];
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            LOG.info(encoding != null ? "Detected encoding: " + encoding : "No encoding detected.");
            detector.reset();
            return encoding;
        }
    }

    private static String findSeparator(String string) {
        string = removeCharacters(string, '"');
        Map<Character, Integer> map = new HashMap<>();
        while (!string.isEmpty()) {
            int count = countCharacter(string, string.charAt(0));
            if (count > max) {
                max = count;
            }
            map.put(string.charAt(0), count);
            string = removeCharacters(string, string.charAt(0));
        }
        return Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey().toString();
    }

    private static String getFirstRowsInFile(File file, String charset) throws IOException {
        Path path = Path.of(file.getAbsolutePath());
        return Files.readAllLines(path, Charset.forName(charset)).stream()
                .limit(10)
                .collect(Collectors.joining());
    }

    private static String removeCharacters(String string, char c) {
        return string.chars()
                .filter(ch -> ch != c)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static int countCharacter(String string, char c) {
        return (int) string.chars().filter(ch -> ch == c).count();
    }

    private static Path getPath(String file) {
        return Path.of(TEMP_PATH, file);
    }
}
