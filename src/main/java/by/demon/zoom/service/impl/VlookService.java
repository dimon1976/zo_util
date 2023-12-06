package by.demon.zoom.service.impl;

import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class VlookService implements FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VlookService.class);
    private final List<String> header = Arrays.asList("ID", "BAR", "URL");
    private final DataToExcel<VlookBarDTO> dataToExcel;
    private final DataDownload dataDownload;


    public VlookService(DataToExcel<VlookBarDTO> dataToExcel, DataDownload dataDownload) {
        this.dataToExcel = dataToExcel;
        this.dataDownload = dataDownload;
    }

    @Override
    public Collection readFiles(List<File> files, String... additionalParams) {
        Map<String, Set<String>> mapOne = new HashMap<>();
        Map<String, Set<String>> mapTwo = new HashMap<>();
        List<VlookBarDTO> result = new ArrayList<>();
        for (File file : files) {
            readDataFromFile(file)
                    .forEach(objects -> {
                        addMapOne(objects, mapOne);
                        addMapTwo(objects, mapTwo);
                    });

            mapTwo.forEach((keyTwo, value) -> mapOne.getOrDefault(keyTwo, Collections.emptySet())
                    .forEach(keyOne -> value.forEach(url -> result.add(new VlookBarDTO(keyOne, keyTwo, url)))));

        }

//            dataToExcel.exportToExcel(header, result, outputStream, skip);
//            download(response, path, Globals.SUFFIX_XLSX);
        return result;
//        log.info("Data exported successfully to Excel: {}", path.getFileName());
    }
//        catch (IOException e) {
//            log.error("Error exporting data to Excel: {}", e.getMessage(), e);
//            throw e;
//        }

//        return path.toAbsolutePath().toString();
//}

    @Override
    public void download(HttpServletResponse response, Path path, String format, String... additionalParams) throws IOException {
//        Path path1 = DataDownload.getPath("vlook-result", DataDownload.setSuffix(format));
        try (FileInputStream is = new FileInputStream(path.toAbsolutePath().toString())) {
            dataDownload.downloadExcel(path, is, response);
        }
    }

    @Override
    public void save(Collection<T> collection) {

    }

    @Override
    public Collection<T> listAll() {
        return null;
    }

    private void addMapOne(List<Object> list, Map<String, Set<String>> mapOne) {
        if (list.size() >= 2) {
            String id = String.valueOf(list.get(0));
            if (!id.isEmpty()) {
                String bar = String.valueOf(list.get(1));
                if (bar.length() > 13) {
                    String[] strings = bar.split(",");
                    Arrays.stream(strings)
                            .map(str -> str.replaceAll("\\D", ""))
                            .filter(barClear -> barClear.length() == 13)
                            .forEach(barClear -> mapOne.computeIfAbsent(barClear, k -> new HashSet<>()).add(id));
                } else {
                    lengthCheck(mapOne, id, bar);
                }
            }
        }
    }

    private void lengthCheck(Map<String, Set<String>> hashMap, String data, String verification) {
        if (verification.length() == 13) {
            hashMap.computeIfAbsent(verification, k -> new HashSet<>()).add(data);
        }
    }

    private void addMapTwo(List<Object> list, Map<String, Set<String>> mapTwo) {
        if (list.size() > 3) {
            String url = String.valueOf(list.get(4));
            if (url.length() > 10) {
                String bar = String.valueOf(list.get(3));
                if (bar.length() > 13) {
                    String[] strings = bar.split(",");
                    Arrays.stream(strings)
                            .map(string -> string.replaceAll("\\D", ""))
                            .forEach(barClear -> lengthCheck(mapTwo, url, barClear));

                } else {
                    lengthCheck(mapTwo, url, bar);
                }
            }
        }
    }
}
