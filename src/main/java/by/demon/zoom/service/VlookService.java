package by.demon.zoom.service;

import by.demon.zoom.domain.VlookBar;
import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.ExcelUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelUtil.readExcel;
import static by.demon.zoom.util.Globals.VLOOK_RESULT;

@Getter
@Setter
@NoArgsConstructor
@Service
public class VlookService {

    private final String[] header = {"ID", "BAR", "URL"};
    private MappingUtils mappingUtils;

    private ExcelUtil<VlookBarDTO> excelUtil;

    public VlookService(MappingUtils mappingUtils, ExcelUtil<VlookBarDTO> excelUtil) {
        this.mappingUtils = mappingUtils;
        this.excelUtil = excelUtil;
    }


    public String export(String filePath, File file, HttpServletResponse response) throws IOException {
        List<List<Object>> list = readExcel(file);
        short skip = 0;

        // key = BAR value = Set[id]
        HashMap<String, HashSet<String>> mapOne = new HashMap<>();
        // key = BAR value = set[url]
        HashMap<String, HashSet<String>> mapTwo = new HashMap<>();

        List<VlookBar> result = new ArrayList<>();

        for (List<Object> objects : list) {
            addMapOne(objects, mapOne);
            addMapTwo(objects, mapTwo);
        }

        for (Map.Entry<String, HashSet<String>> data : mapTwo.entrySet()) {
            if (mapOne.containsKey(data.getKey())) {
                Set<String> setId = mapOne.get(data.getKey());
                String bar = data.getKey();
                HashSet<String> setUrl = data.getValue();
                for (String id : setId) {
                    VlookBar vlookBar = new VlookBar();
                    vlookBar.setId(id);
                    vlookBar.setBar(bar);
                    vlookBar.setUrl(setUrl);
                    result.add(vlookBar);
                }
            }
        }
        // **https://stackoverflow.com/a/25147125/21789158 - ответ по соединению List<List<>> в один List в стриме
        Collection<VlookBarDTO> vlookBarDTOS = result.stream()
                .map(MappingUtils::mapToVlookBarDto)
                .collect(Collectors.toList())
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            excelUtil.exportExcel(header, vlookBarDTOS, outputStream, skip);
            excelUtil.download(VLOOK_RESULT, filePath, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }


    private void addMapOne(List<Object> list, HashMap<String, HashSet<String>> mapOne) {
        if (list.size() >= 2) {  //Проверка на размер ячейки коллекции
            String id = String.valueOf(list.get(0));
            if (id.length() > 0) {
                String bar = String.valueOf(list.get(1));
                if (bar.length() > 13) {
                    String[] strings = bar.split(",");
                    HashSet<String> idSet = new HashSet<>();
                    for (String str : strings) {
                        String barClear = str.replaceAll("\\D", "");
                        if (barClear.length() == 13) {
                            if (mapOne.containsKey(barClear)) {
                                mapOne.get(barClear).add(id);
                            } else {
                                idSet.add(id);
                                mapOne.put(barClear, idSet);
                            }
                        }
                    }

                } else {
                    lengthCheck(mapOne, id, bar);
                }
            }
        }
    }

    private void lengthCheck(HashMap<String, HashSet<String>> hashMap, String data, String verification) {
        if (verification.length() == 13) {
            if (hashMap.containsKey(verification)) {
                hashMap.get(verification).add(data);
            } else {
                HashSet<String> idSet = new HashSet<>();
                idSet.add(data);
                hashMap.put(verification, idSet);
            }
        }
    }

    private void addMapTwo(List<Object> list, HashMap<String, HashSet<String>> mapTwo) {
        if (list.size() > 3) {
            String url = String.valueOf(list.get(4));
            if (url.length() > 10) {
                String bar = String.valueOf(list.get(3));
                if (bar.length() > 13) {
                    String[] strings = bar.split(",");
                    for (String string : strings) {
                        lengthCheck(mapTwo, url, string);
                    }
                } else lengthCheck(mapTwo, url, bar);
            }
        }
    }

}
