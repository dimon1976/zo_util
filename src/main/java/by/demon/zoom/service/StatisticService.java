package by.demon.zoom.service;

import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static by.demon.zoom.mapper.MappingUtils.ifExistCompetitor;
import static by.demon.zoom.mapper.MappingUtils.listUsers;

@Service
public class StatisticService {
    private static final List<String> header = Arrays.asList("Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл.");
    private final ExcelUtil<Object> excelUtil;

    public StatisticService(ExcelUtil<Object> excelUtil) {
        this.excelUtil = excelUtil;
    }


    public String export(String filePath, File file, HttpServletResponse response, String showSource, String sourceReplace, String showCompetitorUrl, String showDateAdd) throws IOException {
        List<List<Object>> originalWb = ExcelUtil.readExcel(file);
        List<Integer> columns = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 19, 20, 22, 23, 24);
        List<Integer> newColumn = getColumnList(showSource, showCompetitorUrl,showDateAdd, columns);
        List<List<Object>> resultTest = getResultList(originalWb, newColumn, sourceReplace);
        try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
            List<String> newHeader = new ArrayList<>(header);
            short skip = 1;
            if (showCompetitorUrl != null) {
                newHeader.add("URL");
            }
            if (showSource != null) {
                newHeader.add("Добавил");
            }
            if (showDateAdd != null) {
                newHeader.add("Добавил");
            }
            excelUtil.exportExcel(newHeader, resultTest, out, skip);
            excelUtil.download(file.getName(), filePath, response);
        }
        return filePath;
    }

    private static List<Integer> getColumnList(String showSource, String showCompetitorUrl, String showDateAdd, List<Integer> source) {
        List<Integer> targetList = new ArrayList<>(source);
        if (showCompetitorUrl != null) {
            targetList.add(27);
        }
        if (showSource != null) {
            targetList.add(28);
        }
        if (showDateAdd !=null){
            targetList.add(29);
        }
        return targetList;
    }

    private static List<List<Object>> getResultList(List<List<Object>> list, List<Integer> columnList, String sourceReplace) {
        List<List<Object>> newList = new LinkedList<>();
        int counter = 0;
        for (int i = 0; counter < list.size(); i++) {
            counter++;
            List<Object> linked = new LinkedList<>();
            getRowList(linked, columnList, list.get(i), sourceReplace);
            newList.add(linked);
        }
        return newList;
    }

    private static void getRowList(List<Object> linked, List<Integer> columnList, List<Object> row, String sourceReplace) {
        Object value;
        for (int j = 0; j <= row.size(); j++) {
            if (ifExistField(j, columnList)) {
                value = row.get(j);
                if (value == null) {
                    linked.add("");
                    break;
                } else if (sourceReplace != null && j == 28) {
                    if (!ifExistCompetitor((String) row.get(j), listUsers)) {
                        linked.add("manager");
                    }
                } else {
                    linked.add(value);
                }
            }
        }
    }

    public static Boolean ifExistField(int i, List<Integer> listColumns) {
        return listColumns.stream()
                .anyMatch(numberColumns -> numberColumns == i);
    }


//    private List<Product> getResultList(List<List<Object>> list) {
//        ArrayList<Product> resultList = new ArrayList<>();
//        for (List<Object> str : list) {
//            Product product = new Product();
//            product.setClient(String.valueOf(str.get(0)));
//            product.setIdLink(String.valueOf(str.get(1)));
//            product.setId(String.valueOf(str.get(2)));
//            product.setParentCategory(String.valueOf(str.get(3)));
//            product.setCategory(String.valueOf(str.get(4)));
//            product.setVendor(String.valueOf(str.get(5)));
//            product.setModel(String.valueOf(str.get(6)));
//            product.setProductCode(String.valueOf(str.get(7)));
//            product.setBar(String.valueOf(str.get(8)));
//            product.setStatus(String.valueOf(str.get(9)));
//            product.setCompetitorPrice(String.valueOf(str.get(12)));
//            product.setCompetitorModel(String.valueOf(str.get(19)));
//            product.setCompetitorProductCode(String.valueOf(str.get(20)));
//            product.setCompetitorId(String.valueOf(str.get(22)));
//            product.setCompetitor(String.valueOf(str.get(23)));
//            product.setOn(String.valueOf(str.get(24)));
//            product.setUserAdd(String.valueOf(str.get(28)));
//            product.setCompetitorUrl(String.valueOf(str.get(27)));
//            resultList.add(product);
//        }
//        return resultList;
//    }

//    private List<StatisticDTO> getDTOList(List<Product> list, String showSource, String sourceReplace, String showCompetitorUrl) {
//        return list.stream()
//                .map((Product product) -> MappingUtils.mapToDetmirDTO(product, showSource, sourceReplace, showCompetitorUrl))
//                .collect(Collectors.toList());
//    }

}

