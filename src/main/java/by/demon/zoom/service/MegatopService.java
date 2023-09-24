package by.demon.zoom.service;

import by.demon.zoom.dao.MegatopCsvRBeanRepository;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class MegatopService {

    private static final DateTimeFormatter MEGATOP_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m");
    private String fileName = "";
    private final List<String> header = Arrays.asList("Категория 1", "Категория", "Высота каблука", "Коллекция", "Конструкция верх", "Материал верха", "Материал подкладки",
            "Ростовка дети", "Цвета", "Сезон", "Конкурент", "ID", "Категория", "Бренд", "Модель", "Артикул", "Цена", "Старая цена", "Ссылка на модель", "Статус");
    private final LocalDate beforeDate = LocalDate.of(2020, 8, 1);
    private final MegatopCsvRBeanRepository megatopCsvRBeanRepository;
    private final ExcelUtil<MegatopDTO> excelUtil;

    public MegatopService(MegatopCsvRBeanRepository megatopCsvRBeanRepository, ExcelUtil<MegatopDTO> excelUtil) {
        this.megatopCsvRBeanRepository = megatopCsvRBeanRepository;
        this.excelUtil = excelUtil;
    }


    public String export(String filePath, File file, HttpServletResponse response) throws IOException {
        fileName = file.getName();
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);
        Collection<Megatop> megatopArrayList = getMegatopList(lists);
        HashSet<MegatopDTO> collect = getMegatopDTOList(megatopArrayList);
        megatopCsvRBeanRepository.saveAll(megatopArrayList);
        try (OutputStream out = Files.newOutputStream(of)) {
            short skipLines = 1;
            excelUtil.exportExcel(header, collect, out, skipLines);
            excelUtil.download(file.getName(), filePath, response);
        }
        return filePath;
    }

    private HashSet<MegatopDTO> getMegatopDTOList(Collection<Megatop> megatopList) {
        HashSet<MegatopDTO> megatopDTOS = new HashSet<>();
        for (Megatop megatop : megatopList) {
            if (megatop.getCompetitor().equals("belwest.by")) {
                MegatopDTO megatopDTO = MappingUtils.mapToMegatopDTO(megatop);
                megatopDTOS.add(megatopDTO);
            } else {
                if (!megatop.getUrl().contains("/ru/") && !megatop.getUrl().contains("/kz/") && !megatop.getDate().toLocalDate().isBefore(beforeDate)) {
                    MegatopDTO megatopDTO = MappingUtils.mapToMegatopDTO(megatop);
                    megatopDTOS.add(megatopDTO);
                }
            }
        }
        return megatopDTOS;
    }

    private Collection<Megatop> getMegatopList(List<List<Object>> lists) {
        Timestamp instant = Timestamp.from(Instant.now());
        HashSet<Megatop> arrayList = new HashSet<>();
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
            megatop.setMegatopId(String.valueOf(str.get(11)));
            megatop.setCategory2(String.valueOf(str.get(12)));
            megatop.setBrand(String.valueOf(str.get(13)));
            megatop.setModel(String.valueOf(str.get(14)));
            megatop.setVendorCode(String.valueOf(str.get(15)));
            megatop.setPrice(String.valueOf(str.get(16)));
            megatop.setOldPrice(String.valueOf(str.get(17)));
            megatop.setUrl(String.valueOf(str.get(18)));
            megatop.setStatus(String.valueOf(str.get(19)));
            if (!String.valueOf(str.get(20)).equals("")) {
                megatop.setDate(DateUtils.getDateTime(String.valueOf(str.get(20)),MEGATOP_PATTERN));
            }
            megatop.setConcatUrlRostovChildren((str.get(18)) + String.valueOf(str.get(7)));
            megatop.setFileName(fileName);
            megatop.setDateTime(instant);
            arrayList.add(megatop);
        }
        return arrayList;
    }
}
