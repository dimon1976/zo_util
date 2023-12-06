package by.demon.zoom.service.impl;

import by.demon.zoom.dao.MegatopRepository;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.StringUtil;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class MegatopService implements FileProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(MegatopService.class);
    private static final DateTimeFormatter MEGATOP_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m");
    private final List<String> header = Arrays.asList("Категория 1", "Категория", "Высота каблука", "Коллекция", "Конструкция верх", "Материал верха", "Материал подкладки",
            "Ростовка дети", "Цвета", "Сезон", "Конкурент", "ID", "Категория", "Бренд", "Модель", "Артикул", "Цена", "Старая цена", "Ссылка на модель", "Статус");
    private final LocalDate beforeDate = LocalDate.of(2020, 8, 1);
    private final MegatopRepository megatopRepository;
    private final DataToExcel<MegatopDTO> dataToExcel;
    private final DataDownload dataDownload;

    public MegatopService(MegatopRepository megatopRepository, DataToExcel<MegatopDTO> dataToExcel, DataDownload dataDownload) {
        this.megatopRepository = megatopRepository;
        this.dataToExcel = dataToExcel;
        this.dataDownload = dataDownload;
    }

//    public String export(List<File> files, String label) throws IOException {
//        for (File file : files) {
//            List<List<Object>> lists = readDataFromFile(file);
//            try {
//                Files.deleteIfExists(file.toPath());
//                LOG.info("File {} remove successfully.", file.getAbsolutePath());
//            } catch (IOException e) {
//                LOG.error("File {} remove failed.", file.getAbsolutePath());
//                throw new IOException("File {} remove failed.");
//            }
//            Collection<Megatop> megatopArrayList = getMegatopList(lists, label, file);
//            megatopRepository.saveAll(megatopArrayList);
//            LOG.info("File {} processed and saved successfully.", file.getName());
//        }
//        return "All files processed and saved successfully.";
//    }

    @Override
    public void download(HttpServletResponse response, Path path, String format, String... additionalParameters) throws IOException {
        try {
            List<Megatop> megatopByLabel = getMegatopByLabel(additionalParameters[0]);
            HashSet<MegatopDTO> megatopDTOList = getMegatopDTOList(megatopByLabel);
            // Экспортируем данные в Excel
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 FileInputStream is = new FileInputStream(path.toAbsolutePath().toString())) {
                dataToExcel.exportToExcel(header, megatopDTOList, out, 0);
                byte[] data = out.toByteArray();
                Files.write(path, data);
                // Скачиваем файл
                dataDownload.downloadExcel(path, is, response);
                DataDownload.cleanupTempFile(path);
                LOG.info("Data exported successfully to Excel: {}", path.getFileName().toString());
            }
        } catch (IOException e) {
            LOG.error("Error exporting data to Excel: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Collection readFiles(List<File> files, String... additionalParams) throws IOException {
        Collection<Megatop> megatopArrayList = new ArrayList<>();
        for (File file : files) {
            List<List<Object>> lists = readDataFromFile(file);
            try {
                Files.deleteIfExists(file.toPath());
                LOG.info("File {} remove successfully.", file.getAbsolutePath());
            } catch (IOException e) {
                LOG.error("File {} remove failed.", file.getAbsolutePath());
                throw new IOException("File {} remove failed.");
            }
            megatopArrayList = getMegatopList(lists, additionalParams[0], file);
            megatopRepository.saveAll(megatopArrayList);
            LOG.info("File {} processed and saved successfully.", file.getName());
        }
        LOG.info("All files processed and saved successfully.");
        return megatopArrayList;
    }

    @Override
    public void save(Collection<T> collection) {

    }

    @Override
    public Collection<T> listAll() {
        return null;
    }

    private List<Megatop> getMegatopByLabel(String label) {
        return megatopRepository.findByLabel(label);
    }

    private HashSet<MegatopDTO> getMegatopDTOList(Collection<Megatop> megatopList) {
        return megatopList.stream()
                .filter(megatop -> "belwest.by".equals(megatop.getCompetitor()) ||
                        (!megatop.getUrl().contains("/ru/") && !megatop.getUrl().contains("/kz/") &&
                                !megatop.getDate().toLocalDate().isBefore(beforeDate)))
                .map(MappingUtils::mapToMegatopDTO)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Collection<Megatop> getMegatopList(List<List<Object>> lists, String label, File file) {
        return lists.stream()
                .filter(str -> !"Категория 1".equals(str.get(0)))
                .map(str -> createMegatopFromList(str, Timestamp.from(Instant.now()), label, file))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Megatop createMegatopFromList(List<Object> str, Timestamp instant, String label, File file) {
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
        megatop.setLabel(label);
        megatop.setFileName(file.getName());
        megatop.setDateTime(instant);
        return megatop;
    }

    // Генерация уникальной метки
    public String generateLabel() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy:hh-mm-ss"));
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }


//    public String download(File tempFile, HttpServletResponse response, String... additionalParams) throws IOException {
//        return null;
//    }

    public List<String> getLatestLabels() {
        // Получаем последние 10 сохраненных меток из базы данных
        Pageable pageable = PageRequest.of(0, 10);
        return megatopRepository.findTop10DistinctLabels(pageable);
    }
}
