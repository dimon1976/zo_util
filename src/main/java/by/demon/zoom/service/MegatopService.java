package by.demon.zoom.service;

import by.demon.zoom.domain.Megatop;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class MegatopService {

    private final String[] header = {"Категория 1", "Категория", "Высота каблука", "Коллекция", "Конструкция верх", "Материал верха", "Материал подкладки",
            "Ростовка дети", "Цвета", "Сезон", "Конкурент", "ID", "Категория", "Бренд", "Модель", "Артикул", "Цена", "Старая цена", "Ссылка на модель", "Статус"};
    private final LocalDate beforeDate = LocalDate.of(2020, 8, 1);
    private final Collection<Megatop> megatopArrayList = new ArrayList<>();

    @Autowired
    private ExcelUtil<MegatopDTO> excelUtil;


    public String getList(String filePath, File file, HttpServletResponse response) throws IOException {
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);

        long start = System.currentTimeMillis();

        getMegatopList(lists);
        System.out.println("Время работы - лист Megatop - " + (System.currentTimeMillis() - start) / 1000);

        long start2 = System.currentTimeMillis();

        short skipLines = 1;
        getMegatopDTOList(filePath, file, response, of, lists, skipLines, start2);
        return filePath;
    }

    private void getMegatopDTOList(String filePath, File file, HttpServletResponse response, Path of, List<List<Object>> lists, short skip, long start2) throws IOException {
        if (lists.size() == 22) {
            List<MegatopDTO> collect = megatopArrayList.stream()
                    .filter(l -> !l.getUrl().contains("/ru/") && !l.getUrl().contains("/kz/") && !l.getDate().toLocalDate().isBefore(beforeDate))
                    .map(MappingUtils::mapToMegatopDTO)
                    .collect(Collectors.toList());


            System.out.println("Время работы - лист MegatopDTO - " + (System.currentTimeMillis() - start2) / 1000);
            try (OutputStream out = Files.newOutputStream(of)) {

                long start3 = System.currentTimeMillis();
                excelUtil.exportExcel(header, collect, out, skip);
                System.out.println("Время работы - export - " + (System.currentTimeMillis() - start3) / 1000);

                long start4 = System.currentTimeMillis();
                excelUtil.download(file.getName(), filePath, response);
                System.out.println("Время работы - download - " + (System.currentTimeMillis() - start4) / 1000);
            }
        } else {
            List<MegatopDTO> collect = megatopArrayList.stream()
                    .filter(l -> !l.getUrl().contains("/ru/") && !l.getUrl().contains("/kz/"))
                    .map(MappingUtils::mapToMegatopDTO)
                    .collect(Collectors.toList());
            System.out.println("Время работы - лист MegatopDTO - " + (System.currentTimeMillis() - start2) / 1000);
            try (OutputStream out = Files.newOutputStream(of)) {

                long start3 = System.currentTimeMillis();
                excelUtil.exportExcel(header, collect, out, skip);
                System.out.println("Время работы - export - " + (System.currentTimeMillis() - start3) / 1000);

                long start4 = System.currentTimeMillis();
                excelUtil.download(file.getName(), filePath, response);
                System.out.println("Время работы - download - " + (System.currentTimeMillis() - start4) / 1000);
            }
        }
    }

    private void getMegatopList(List<List<Object>> lists) {
        for (List<Object> str : lists) {
            if (String.valueOf(str.get(0)).equals("Категория 1")) {
                continue;
            }
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
                if (!String.valueOf(str.get(20)).equals("")) {
                    megatop.setDate(DateUtils.getDateTime(String.valueOf(str.get(20))));
                }
            }
            megatop.setConcatUrlRostovChildren((str.get(18)) + String.valueOf(str.get(7)));
            megatopArrayList.add(megatop);
        }
    }

}
