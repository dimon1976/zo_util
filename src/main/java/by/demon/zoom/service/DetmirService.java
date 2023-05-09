package by.demon.zoom.service;

import by.demon.zoom.domain.DetmirStats;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class DetmirService {
    private final String[] header = {"Клиент", "ID связи", "ID клиента", "Верхняя категория клиента", "Категория клиента", "Бренд клиента",
            "Модель клиента", "Код производителя клиента", "Штрих-код клиента", "Статус клиента", "Цена конкурента",
            "Модель конкурента", "Код производителя конкурента", "ID конкурента", "Конкурент", "Конкурент вкл."};
    private final ExcelUtil<DetmirStats> excelUtil;

    public DetmirService(ExcelUtil<DetmirStats> excelUtil) {
        this.excelUtil = excelUtil;
    }


    public String export(String filePath, File file, HttpServletResponse response) throws IOException {
        List<List<Object>> originalWb = ExcelUtil.readExcel(file);
        Collection<DetmirStats> result = getResultList(originalWb);
        try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
            short skip = 1;
            excelUtil.exportExcel(header, result, out, skip);
            excelUtil.download(file.getName(), filePath, response);
        }
        return filePath;
    }

    private Collection<DetmirStats> getResultList(List<List<Object>> list) {
        ArrayList<DetmirStats> resultList = new ArrayList<>();
        for (List<Object> str : list) {
            DetmirStats detmirStat = new DetmirStats();
            detmirStat.setClient(String.valueOf(str.get(0)));
            detmirStat.setId(String.valueOf(str.get(1)));
            detmirStat.setClientId(String.valueOf(str.get(2)));
            detmirStat.setParentCategory(String.valueOf(str.get(3)));
            detmirStat.setCategory(String.valueOf(str.get(4)));
            detmirStat.setVendor(String.valueOf(str.get(5)));
            detmirStat.setModel(String.valueOf(str.get(6)));
            detmirStat.setProductCode(String.valueOf(str.get(7)));
            detmirStat.setBar(String.valueOf(str.get(8)));
            detmirStat.setClientStatus(String.valueOf(str.get(9)));
            detmirStat.setCompetitorPrice(String.valueOf(str.get(12)));
            detmirStat.setCompetitorModel(String.valueOf(str.get(19)));
            detmirStat.setCompetitorProductCode(String.valueOf(str.get(20)));
            detmirStat.setCompetitorId(String.valueOf(str.get(22)));
            detmirStat.setCompetitor(String.valueOf(str.get(23)));
            detmirStat.setOn(String.valueOf(str.get(24)));
            resultList.add(detmirStat);
        }
        return resultList;
    }

}

