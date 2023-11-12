package by.demon.zoom.service.impl;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelUtil.readExcel;
import static by.demon.zoom.util.Globals.VLOOK_RESULT;

@Service
public class VlookService implements FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VlookService.class);
    private final List<String> header = Arrays.asList("ID", "BAR", "URL");
    private final ExcelUtil<VlookBarDTO> excelUtil;

    public VlookService(ExcelUtil<VlookBarDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }


    public String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        List<List<Object>> list = readExcel(file);
        short skip = 0;
        Map<String, Set<String>> mapOne = new HashMap<>();
        Map<String, Set<String>> mapTwo = new HashMap<>();
        List<Product> result = new ArrayList<>();

        list.forEach(objects -> {
            addMapOne(objects, mapOne);
            addMapTwo(objects, mapTwo);
        });

        mapTwo.forEach((key, value) -> mapOne.getOrDefault(key, Collections.emptySet())
                .forEach(id -> result.add(new Product(id, key, new HashSet<>(value)))));

        // **https://stackoverflow.com/a/25147125/21789158 - ответ по соединению List<List<>> в один List в стриме
        Collection<VlookBarDTO> vlookBarDTOS = result.stream()
                .map(MappingUtils::mapToVlookBarDto)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            excelUtil.exportExcel(header, vlookBarDTOS, outputStream, skip);
            excelUtil.download(VLOOK_RESULT, filePath, response);
            log.info("Data exported successfully to Excel: {}", filePath);
        } catch (IOException e) {
            log.error("Error exporting data to Excel: {}", e.getMessage(), e);
            throw e;
        }

        return filePath;
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
                } else lengthCheck(mapTwo, url, bar);
            }
        }
    }
}