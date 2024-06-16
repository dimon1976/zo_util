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

import java.io.*;
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
    private static final int MAX = 0;


    public static List<List<Object>> readCSV(File file) throws IOException, CsvValidationException {
        LOG.info("Processing file: {}", file.getName());

        Path path = getPath(file.getName());
        String charset = detectCharset(path.toFile());
        char delimiter = findDelimiter(getFirstRows(path, charset));

        List<List<Object>> csvData = new ArrayList<>();
        try (CSVReader csvReader = createCsvReader(path, charset, delimiter)) {
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                csvData.add(new ArrayList<>(Arrays.asList(nextLine)));
            }
        } catch (IOException | CsvValidationException e) {
            LOG.error("Error reading CSV file: {}", e.getMessage(), e);
            throw e;
        }

        return csvData;
    }


    private static CSVReader createCsvReader(Path path, String charset, char delimiter) throws IOException {
        CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        Reader reader = Files.newBufferedReader(path, Charset.forName(charset));
        return new CSVReaderBuilder(reader).withSkipLines(0).withCSVParser(parser).build();
    }

    private static String detectCharset(File file) throws IOException {
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
            return encoding != null ? encoding : Charset.defaultCharset().name();
        }
    }


    private static char findDelimiter(String string) {
        char[] possibleDelimiters = {';', ',', '\t'};
        Map<Character, Integer> delimiterCounts = new HashMap<>();

        for (char delimiter : possibleDelimiters) {
            int count = countCharacter(string, delimiter);
            delimiterCounts.put(delimiter, count);
        }

        return delimiterCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new IllegalStateException("No delimiter found"))
                .getKey();
    }

    private static String getFirstRows(Path path, String charset) throws IOException {
        return Files.readAllLines(path, Charset.forName(charset)).stream()
                .limit(5)
                .collect(Collectors.joining());
    }

    private static int countCharacter(String string, char c) {
        return (int) string.chars().filter(ch -> ch == c).count();
    }

    private static Path getPath(String file) {
        return Path.of(TEMP_PATH, file);
    }
}
