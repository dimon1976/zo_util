package by.demon.zoom.util;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.Globals.TEMP_PATH;
import static com.opencsv.ICSVWriter.DEFAULT_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.DEFAULT_LINE_END;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
@Service
public class CsvUtil {
    private final static String header = "\"Клиент\";\"ID связи\";\"ID клиента\";\"Верхняя категория клиента\";\"Категория клиента\";\"Бренд клиента\";\"Модель клиента\";" +
            "\"Код производителя клиента\";\"Штрих-код клиента\";\"Статус клиента\";\"Цена конкурента\";\"Модель конкурента\";\"Код производителя конкурента\";\"ID конкурента\";" +
            "\"Конкурент\";\"Конкурент вкл.\"\n";

    private static int max = 0;

    /*Оригинальный метод чтения CSV*/
    public static List<String[]> readCsv(File file) throws IOException, CsvException {
        log.info(String.format("file %s processing", file.getName()));
        Path path = getPath(file.getName());
        String charset = getNameCharset(path);
        String separator = getSeparator(file, charset);
        return getStrings(path, charset, separator);
    }


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
                List<Object> row = new ArrayList<>();
                for (String cell : nextLine) {
                    row.add(cell);
                }
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

    private static List<String[]> getStrings(Path path, String charset, String separator) throws IOException, CsvException {
        try (Reader reader = Files.newBufferedReader(path, Charset.forName(charset))) {
            CSVParser parser = getCsvParser(separator);
            CSVReader csvReader = getCsvReader(reader, parser);
            return getStrings(csvReader);
        }
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

    private static List<String[]> getStrings(CSVReader csvReader) throws IOException, CsvException {
        return csvReader.readAll().stream()
//                .filter(this::filterString)
                .skip(1)
//                .map(DetmirRow::new)
//                .map(CsvRow::toCsvArrays)
                .collect(Collectors.toList());
    }

    public void write(List<String[]> strings, File file) throws IOException {
        Path pathOut = getPathOut(file);
        Files.write(pathOut, header.getBytes("Windows-1251"), CREATE);
        CSVWriter csvWriter = null;
        try {
            csvWriter = new CSVWriter(new FileWriter(pathOut.toString(), Charset.forName("Windows-1251"), true)
                    , getSeparator(file, getCharset(file)).charAt(0), '"', DEFAULT_ESCAPE_CHARACTER, DEFAULT_LINE_END);
            csvWriter.writeAll(strings);
        } catch (IOException | RuntimeException e) {
            log.info(String.format("Error Save %s", file.getName()));
            e.printStackTrace();
        } finally {
            try {
                assert csvWriter != null;
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Path getPathOut(File file) {
        return Path.of(TEMP_PATH, file.getName().replace(".csv", "_out.csv"));
    }
}
