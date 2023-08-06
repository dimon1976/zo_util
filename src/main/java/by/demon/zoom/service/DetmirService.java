package by.demon.zoom.service;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.DetmirDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetmirService {
    private final String[] header = {"Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл."};
    private final String[] header2 = {"Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл.","Добавил"};
    private final ExcelUtil<DetmirDTO> excelUtil;

    public DetmirService(ExcelUtil<DetmirDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }


    public String export(String filePath, File file, HttpServletResponse response, String showSource, String sourceReplace) throws IOException {
        List<List<Object>> originalWb = ExcelUtil.readExcel(file);
        List<Product> result = getResultList(originalWb);
        List<DetmirDTO> resultDTO = getDTOList(result, showSource, sourceReplace);
        try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
            if (showSource != null || sourceReplace != null) {
                short skip = 1;
                excelUtil.exportExcel(header2, resultDTO, out, skip);
                excelUtil.download(file.getName(), filePath, response);
            } else {
                short skip = 1;
                excelUtil.exportExcel(header, resultDTO, out, skip);
                excelUtil.download(file.getName(), filePath, response);
            }
        }
        return filePath;
    }

    private List<Product> getResultList(List<List<Object>> list) {
        ArrayList<Product> resultList = new ArrayList<>();
        for (List<Object> str : list) {
            Product product = new Product();
            product.setClient(String.valueOf(str.get(0)));
            product.setIdLink(String.valueOf(str.get(1)));
            product.setId(String.valueOf(str.get(2)));
            product.setParentCategory(String.valueOf(str.get(3)));
            product.setCategory(String.valueOf(str.get(4)));
            product.setVendor(String.valueOf(str.get(5)));
            product.setModel(String.valueOf(str.get(6)));
            product.setProductCode(String.valueOf(str.get(7)));
            product.setBar(String.valueOf(str.get(8)));
            product.setStatus(String.valueOf(str.get(9)));
            product.setCompetitorPrice(String.valueOf(str.get(12)));
            product.setCompetitorModel(String.valueOf(str.get(19)));
            product.setCompetitorProductCode(String.valueOf(str.get(20)));
            product.setCompetitorId(String.valueOf(str.get(22)));
            product.setCompetitor(String.valueOf(str.get(23)));
            product.setOn(String.valueOf(str.get(24)));
            product.setUserAdd(String.valueOf(str.get(28)));
            resultList.add(product);
        }
        return resultList;
    }

    private List<DetmirDTO> getDTOList(List<Product> list, String showSource, String sourceReplace) {
        return list.stream()
                .map((Product product) -> MappingUtils.mapToDetmirDTO(product, showSource, sourceReplace))
                .collect(Collectors.toList());
    }

}

