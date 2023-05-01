package by.demon.zoom.service;

import by.demon.zoom.domain.DetmirStats;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class MegatopService {

    private final static LocalDate beforeDate = LocalDate.of(2020, 8, 1);

    @Autowired
    private ExcelUtil<MegatopDTO> excelUtil;


    public String getList(String filePath, File file, HttpServletResponse response) throws IOException {
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);
        Collection<Megatop> megatopArrayList = new ArrayList<>();
        short skip = 1;
        String[] header = {"Категория 1", "Категория", "Высота каблука", "Коллекция", "Конструкция верх", "Материал верха", "Материал подкладки",
                "Ростовка дети", "Цвета", "Сезон", "Конкурент", "ID", "Категория", "Бренд", "Модель", "Артикул", "Цена", "Старая цена", "Ссылка на модель", "Статус"};

        for (List<Object> str : lists) {
            Megatop megatop = new Megatop();
            megatop.setCategory1(String.valueOf(str.get(0)));
            megatop.setCategory(String.valueOf(str.get(1)));
            megatop.setHeelHeight(String.valueOf(str.get(2)));
            megatop.setCollection(String.valueOf(str.get(3)));
            megatop.setUpperConstruction(String.valueOf(str.get(4)));
            megatop.setUpperMaterial(String.valueOf(str.get(5)));
            megatop.setLiningMaterial(String.valueOf(str.get(6)));
            megatop.setRostovChildren(String.valueOf(str.get(7)));
            megatop.setColors(String.valueOf(str.get(8)));
            megatop.setSeason(String.valueOf(str.get(9)));
            megatop.setCompetitor(String.valueOf(str.get(10)));
            megatop.setId(String.valueOf(str.get(11)));
            megatop.setCategory2(String.valueOf(str.get(12)));
            megatop.setBrand(String.valueOf(str.get(13)));
            megatop.setModel(String.valueOf(str.get(14)));
            megatop.setVendorCode(String.valueOf(str.get(15)));
            megatop.setPrice(String.valueOf(str.get(16)));
            megatop.setOldPrice(String.valueOf(str.get(17)));
            megatop.setUrl(String.valueOf(str.get(18)));
            megatop.setStatus(String.valueOf(str.get(19)));
            megatop.setVendorCode(String.valueOf(str.get(20)));
            if (str.size() == 22) {
                megatop.setDate(DateUtils.getDateTime(String.valueOf(str.get(21))));
            }
            megatop.setConcatUrlRostovChildren((str.get(18)) + String.valueOf(str.get(7)));
            megatopArrayList.add(megatop);
        }

        if (lists.size() == 22) {
            List<MegatopDTO> collect = megatopArrayList.stream()
                    .filter(l -> !l.getUrl().contains("/ru/") && !l.getUrl().contains("/kz/") && !l.getDate().toLocalDate().isBefore(beforeDate))
                    .map(MappingUtils::mapToMegatopDTO)
                    .collect(Collectors.toList());
            try (OutputStream out = Files.newOutputStream(of)) {
                excelUtil.exportExcel(header, collect, out, skip);
                excelUtil.download(file.getName(), filePath, response);
            }
        } else {
            List<MegatopDTO> collect = megatopArrayList.stream()
                    .filter(l -> !l.getUrl().contains("/ru/") && !l.getUrl().contains("/kz/"))
                    .map(MappingUtils::mapToMegatopDTO)
                    .collect(Collectors.toList());
            try (OutputStream out = Files.newOutputStream(of)) {
                excelUtil.exportExcel(header, collect, out, skip);
                excelUtil.download(file.getName(), filePath, response);
            }
        }
        return filePath;
    }

}
