package by.demon.zoom.service.impl;

import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class VlookService implements FileProcessingService<VlookBarDTO> {

    private static final Logger log = LoggerFactory.getLogger(VlookService.class);
    private final List<String> header = Arrays.asList("ID", "BAR", "URL");


    @Override
    public Collection<VlookBarDTO> readFiles(List<File> files, String... additionalParams) {
        Map<String, Set<String>> mapOne = new HashMap<>();
        Map<String, Set<String>> mapTwo = new HashMap<>();
        List<VlookBarDTO> result = new ArrayList<>();
        for (File file : files) {
            try {
                readDataFromFile(file)
                        .forEach(objects -> {
                            try {
                                addMapOne(objects, mapOne);
                                addMapTwo(objects, mapTwo);
                            } catch (Exception e) {
                                log.error("Error processing data:", e);
                            }
                        });

                mapTwo.forEach((keyTwo, value) -> mapOne.getOrDefault(keyTwo, Collections.emptySet())
                        .forEach(keyOne -> value.forEach(url -> result.add(new VlookBarDTO(keyOne, keyTwo, url)))));

            } catch (IOException e) {
                log.error("Error reading data from file: {}", file.getAbsolutePath(), e);
            } finally {
                // Удаление временного файла после обработки
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String save(Collection<VlookBarDTO> collection) {
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
