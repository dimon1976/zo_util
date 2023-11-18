package by.demon.zoom.service.impl;

import by.demon.zoom.dao.UrlFromRepository;
import by.demon.zoom.dao.UrlToRepository;
import by.demon.zoom.domain.vpr.UrlTo;
import by.demon.zoom.domain.vpr.Urlfrom;
import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.FileDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static by.demon.zoom.util.FileDataReader.readExcel;
import static by.demon.zoom.util.Globals.VLOOK_RESULT;

@Service
public class VlookService implements FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VlookService.class);
    private final List<String> header = Arrays.asList("ID", "BAR", "URL");
    private final FileDataReader<VlookBarDTO> excelUtil;
    private final UrlToRepository urlToRepository;
    private final UrlFromRepository urlFromRepository;

    public VlookService(FileDataReader<VlookBarDTO> excelUtil, UrlToRepository urlToRepository, UrlFromRepository urlFromRepository) {
        this.excelUtil = excelUtil;
        this.urlToRepository = urlToRepository;
        this.urlFromRepository = urlFromRepository;
    }

    public String saveAll(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        String fileName = file.getName();
        List<List<Object>> list = readExcel(file);
        if (additionalParams[0].equals("urlFrom")){
            Collection<Urlfrom> urlFromArrayList = convertListUrl(list, Urlfrom.class);
            urlFromRepository.saveAll(urlFromArrayList);
            return fileName;
        } else if (additionalParams[0].equals("urlTo")) {
            Collection<UrlTo> urlToArrayList = convertListUrl(list, UrlTo.class);
            urlToRepository.saveAll(urlToArrayList);
        }
        return fileName;
    }


    private <T> Collection<T> convertListUrl(List<List<Object>> list, Class<T> clazz) {
        Collection<T> collection = new ArrayList<>();
        for (List<Object> sublist : list) {
            if (sublist.size() >= 3) {
                String idFrom = (String) sublist.get(0);
                String bar = (String) sublist.get(1);
                String url = (String) sublist.get(2);
                if (bar.length() >= 13) {
                    for (String substring : bar.split(",")) {
                        String filteredSubstring = substring.replaceAll("[^\\d]", "");
                        if (filteredSubstring.length() == 13) {
                            try {
                                collection.add(clazz.getDeclaredConstructor(String.class, String.class, String.class)
                                        .newInstance(idFrom, bar, url));
                            } catch (Exception e) {
                                log.error("Error occurred while creating Urlfrom object", e);
                            }
                        }
                    }
                }
            }
        }
        return collection;
    }

    public String deleteAll() {
        try {
            urlFromRepository.deleteAllInBatch();
            urlToRepository.deleteAllInBatch();
            log.info("Таблицы успешно очищены");
        } catch (Exception e) {
            log.error("Ошибка при очистке таблиц", e);
        }
        return ("Таблицы успешно очищены");
    }

    public String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        short skip = 0;
        Map<String, Set<String>> mapOne = new HashMap<>();
        Map<String, Set<String>> mapTwo = new HashMap<>();
        List<VlookBarDTO> result = new ArrayList<>();

        readExcel(file)
                .forEach(objects -> {
                    addMapOne(objects, mapOne);
                    addMapTwo(objects, mapTwo);
                });
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            mapTwo.forEach((keyTwo, value) -> mapOne.getOrDefault(keyTwo, Collections.emptySet())
                    .forEach(keyOne -> value.forEach(url -> result.add(new VlookBarDTO(keyOne, keyTwo, url)))));

            excelUtil.exportToExcel(header, result, outputStream, skip);
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

                } else {
                    lengthCheck(mapTwo, url, bar);
                }
            }
        }
    }
}
