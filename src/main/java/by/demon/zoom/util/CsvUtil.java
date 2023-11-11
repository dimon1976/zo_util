package by.demon.zoom.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
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

@Slf4j
@Service
public class CsvUtil {
    private static int max = 0;

    /*Новый метод чтения CSV*/
    public static List<List<Object>> readFile(File file) throws IOException {
        log.info(String.format("file %s processing", file.getName()));
        Path path = getPath(file.getName());
        String charset = getNameCharset(path);
        String separator = getSeparator(file, charset);
        CSVParser parser = getCsvParser(separator);
        List<List<Object>> csvData = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(path, Charset.forName(charset))) {
            CSVReader csvReader = getCsvReader(reader, parser);
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                List<Object> row = new ArrayList<>(Arrays.asList(nextLine));
                csvData.add(row);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        return csvData;
    }

    /**
     * Рекурсивно посчитать вхождение символа в StringBuilder
     */
    public static int countCharacter(StringBuilder string, char c) {
        if (string.length() == 0) return 0;
        return (string.charAt(0) == c)
                ? 1 + countCharacter(string.delete(0, 1), c)
                : countCharacter(string.delete(0, 1), c);
    }

    /**
     * Рекурсивно посчитать вхождение символа c в строку string
     */
    public static int countCharacter(String string, char c) {
        StringBuilder stringBuilder = new StringBuilder(string.subSequence(0, string.length()));
        return countCharacter(stringBuilder, c);
    }

    /**
     * Рекурсивно удалить все вхождения символа "с" в строку string
     */
    public static String removeCharacters(String string, char c) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) != c)
                stringBuilder.append(string.charAt(i));
        }
        return stringBuilder.toString();
    }

    public static String getCharset(File file) throws IOException {

        byte[] buf = new byte[4096];
        InputStream fis = java.nio.file.Files.newInputStream(Paths.get(file.getAbsolutePath()));
        UniversalDetector detector = new UniversalDetector();
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            log.info("Detected encoding = " + encoding);
        } else {
            log.info("No encoding detected.");
        }
        detector.reset();
        return encoding;
    }

    public static String getFirstRowsInFile(File file, String charset) throws IOException {
        Path path = Path.of(file.getAbsolutePath());
        return Files.readAllLines(path, Charset.forName(charset)).stream()
                .limit(10)
                .collect(Collectors.joining());
    }

    private static Path getPath(String file) {
        return Path.of(TEMP_PATH, file);
    }

    private static String getSeparator(File file, String charset) throws IOException {
        return findSeparator(getFirstRowsInFile(file, charset));
    }

    private static String getNameCharset(Path path) throws IOException {
        return getCharset(path.toFile());
    }

    public static String findSeparator(String string) {
        Map<Character, Integer> map = new HashMap<>();
        string = removeCharacters(string, '"');
        while (!string.isEmpty()) {
            int count = countCharacter(string, string.charAt(0));
            if (count > max) {
                max = count;
            }
            Character character = string.charAt(0);
            map.put(character, count);
            string = removeCharacters(string, string.charAt(0));

        }
        return Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey().toString();
    }

    private static CSVReader getCsvReader(Reader reader, CSVParser parser) {
        return new CSVReaderBuilder(reader)
                .withSkipLines(0)
                .withCSVParser(parser)
                .build();
    }

    private static CSVParser getCsvParser(String separator) {
        return new CSVParserBuilder()
                .withSeparator(separator.charAt(0))
                .build();
    }
}
