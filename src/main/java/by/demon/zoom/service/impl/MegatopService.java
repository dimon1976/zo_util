package by.demon.zoom.service.impl;

import by.demon.zoom.dao.MegatopCsvRBeanRepository;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class MegatopService implements FileProcessingService {

    private static final DateTimeFormatter MEGATOP_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m");
    private String fileName = "";
    private final List<String> header = Arrays.asList("Категория 1", "Категория", "Высота каблука", "Коллекция", "Конструкция верх", "Материал верха", "Материал подкладки",
            "Ростовка дети", "Цвета", "Сезон", "Конкурент", "ID", "Категория", "Бренд", "Модель", "Артикул", "Цена", "Старая цена", "Ссылка на модель", "Статус");
    private final LocalDate beforeDate = LocalDate.of(2020, 8, 1);
    private final MegatopCsvRBeanRepository megatopCsvRBeanRepository;
    private final DataToExcel<MegatopDTO> dataToExcel;
    private final DataDownload dataDownload;
    private final Logger log = LoggerFactory.getLogger(MegatopService.class);

    public MegatopService(MegatopCsvRBeanRepository megatopCsvRBeanRepository, DataToExcel<MegatopDTO> dataToExcel, DataDownload dataDownload) {
        this.megatopCsvRBeanRepository = megatopCsvRBeanRepository;
        this.dataToExcel = dataToExcel;
        this.dataDownload = dataDownload;
    }

    public String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        fileName = file.getName();
        Path of = Path.of(filePath);
        List<List<Object>> lists = readDataFromFile(file);
        Collection<Megatop> megatopArrayList = getMegatopList(lists);
        HashSet<MegatopDTO> collect = getMegatopDTOList(megatopArrayList);
        megatopCsvRBeanRepository.saveAll(megatopArrayList);
        try (OutputStream out = Files.newOutputStream(of)) {
            short skipLines = 1;
            dataToExcel.exportToExcel(header, collect, out, skipLines);
            dataDownload.download(file.getName(), filePath, response);
            log.info("Data exported successfully to Excel: {}", filePath);
        } catch (IOException e) {
            log.error("Error exporting data to Excel: {}", e.getMessage(), e);
            throw e;
        }
        return filePath;
    }


    private HashSet<MegatopDTO> getMegatopDTOList(Collection<Megatop> megatopList) {
        return megatopList.stream()
                .filter(megatop -> {
                    boolean condition = "belwest.by".equals(megatop.getCompetitor()) ||
                            (!megatop.getUrl().contains("/ru/") && !megatop.getUrl().contains("/kz/") &&
                                    !megatop.getDate().toLocalDate().isBefore(beforeDate));

                    // Логирование исключенных строк
                    if (!condition) {
                        log.info("Excluded Megatop: {}", megatop);
                    }

                    return condition;
                })
                .map(MappingUtils::mapToMegatopDTO)
                .collect(Collectors.toCollection(HashSet::new));
    }


    private Collection<Megatop> getMegatopList(List<List<Object>> lists) {
//        Timestamp instant = Timestamp.from(Instant.now());
        return lists.stream()
                .filter(str -> !"Категория 1".equals(str.get(0)))
                .map(str -> createMegatopFromList(str, Timestamp.from(Instant.now())))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Megatop createMegatopFromList(List<Object> str, Timestamp instant) {
        Megatop megatop = new Megatop();
        megatop.setCategory1(getStringValue(str, 0));
        megatop.setCategory(getStringValue(str, 1));
        megatop.setHeelHeight(getStringValue(str, 2));
        megatop.setCollection(getStringValue(str, 3));
        megatop.setUpperConstruction(getStringValue(str, 4));
        megatop.setUpperMaterial(getStringValue(str, 5));
        megatop.setLiningMaterial(getStringValue(str, 6));
        megatop.setRostovChildren(getStringValue(str, 7));
        megatop.setColors(getStringValue(str, 8));
        megatop.setSeason(getStringValue(str, 9));
        megatop.setCompetitor(getStringValue(str, 10));
        megatop.setMegatopId(getStringValue(str, 11));
        megatop.setCategory2(getStringValue(str, 12));
        megatop.setBrand(getStringValue(str, 13));
        megatop.setModel(getStringValue(str, 14));
        megatop.setVendorCode(getStringValue(str, 15));
        megatop.setPrice(StringUtil.cleanAndReplace(getStringValue(str, 16), "."));
        megatop.setOldPrice(StringUtil.cleanAndReplace(getStringValue(str, 17), "."));
        megatop.setUrl(getStringValue(str, 18));
        megatop.setStatus(getStringValue(str, 19));
        String dateValue = getStringValue(str, 20);
        megatop.setDate(!dateValue.isEmpty() ? DateUtils.getDateTime(dateValue, MEGATOP_PATTERN) : null);
        megatop.setConcatUrlRostovChildren((getStringValue(str, 18)) + getStringValue(str, 7));
        megatop.setFileName(fileName);
        megatop.setDateTime(instant);
        return megatop;
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }

    @Override
    public String saveAll(String filePath, File transferTo, HttpServletResponse response, String[] additionalParams) {
        return null;
    }

    @Override
    public String deleteAll() {
        return null;
    }
}
