package by.demon.zoom.service.impl;

import by.demon.zoom.dao.MegatopRepository;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.dto.imp.MegatopDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.StringUtil;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class MegatopService implements FileProcessingService<Megatop> {

    private static final Logger log = LoggerFactory.getLogger(MegatopService.class);
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


    public void download(ArrayList<MegatopDTO> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        switch (format) {
            case "excel":
                try {

                    Path path = DataDownload.getPath("data", ".xlsx");
                    // Экспортируем данные в Excel
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportToExcel(header, list, out, 0);
                        byte[] data = out.toByteArray();
                        Files.write(path, data);
                        log.info("Data written to file: {}", path); // Добавьте логирование для проверки
                    }

                    // Скачиваем файл
                    try (FileInputStream is = new FileInputStream(path.toAbsolutePath().toString())) {
                        dataDownload.downloadExcel(path, is, response);
                    }

                    // Удаляем временный файл
                    DataDownload.cleanupTempFile(path);
                    log.info("Data exported successfully to Excel: {}", path.getFileName().toString());
                } catch (IOException e) {
                    log.error("Error exporting data to Excel: {}", e.getMessage(), e);
                    throw e;
                }
                break;
            case "csv":
                Path path1 = DataDownload.getPath("data", ".csv");
                List<String> strings = convert(list);
                dataDownload.downloadCsv(path1, strings, header, response);
                break;
            default:
                break;
        }
    }

    public ArrayList<MegatopDTO> getDto(String... additionalParameters) {
        List<Megatop> megatopByLabel = getMegatopByLabel(additionalParameters[0]);
        return getMegatopDTOList(megatopByLabel);
    }

    private static List<String> convert(List<MegatopDTO> objectList) {
//        return objectList.stream()
////                .filter(Objects::nonNull)
//                .map(MegatopDTO::toCsvRow)
//                .collect(Collectors.toList());

        List<String> collect = new ArrayList<>();

        for (MegatopDTO obj : objectList) {
            try {
                if (obj != null) {  // Фильтруем нулевые элементы
                    String csvRow = obj.toCsvRow();  // Преобразуем в строку CSV
                    collect.add(csvRow);  // Добавляем в список результатов
                }
            } catch (Exception e) {
                // Логируем ошибку
                log.error("Ошибка преобразования объекта в строку CSV", e);
            }
        }

        return collect;
    }

    @Override
    public ArrayList<Megatop> readFiles(List<File> files, String... additionalParams) throws IOException {
        // Используем количество доступных процессоров для определения количества потоков
        int threadCount = Runtime.getRuntime().availableProcessors();

        // Создаем пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Создаем список для сбора результатов обработки файлов
        List<Future<ArrayList<Megatop>>> futures = new ArrayList<>();

        // Отправляем задачу обработки каждого файла в отдельный поток
        for (File file : files) {
            futures.add(executorService.submit(() -> {
                try {
                    log.info("Processing file: {}", file.getName());
                    List<List<Object>> lists = readDataFromFile(file);

                    // Удаляем файл только после успешного чтения данных
                    Files.delete(file.toPath());
                    log.info("File {} removed successfully.", file.getAbsolutePath());

                    ArrayList<Megatop> megatopList = getMegatopList(lists, additionalParams[0], file);
                    save(megatopList);
                    log.info("File {} processed and saved successfully.", file.getName());
                    return megatopList;
                } catch (Exception e) {
                    log.error("Failed to process file: {}", file.getAbsolutePath(), e);
                    throw new RuntimeException("Failed to process file", e); // Оборачиваем в RuntimeException для распространения ошибки
                }
            }));
        }
        // Собераем результаты из всех потоков
        ArrayList<Megatop> allMegatops = new ArrayList<>();
        for (Future<ArrayList<Megatop>> future : futures) {
            try {
                allMegatops.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
            }
        }

        executorService.shutdown();

        return allMegatops;
    }

    @Override
    public String save(ArrayList<Megatop> megatopList) {
        try {
            megatopRepository.saveAll(megatopList);
            log.info("Saved {} Megatop objects", megatopList.size());
            return "";
        } catch (Exception e) {
            log.error("Failed to save Megatop objects", e);
            throw new RuntimeException("Failed to save Megatop objects", e);
        }
    }

    private List<Megatop> getMegatopByLabel(String label) {
        return megatopRepository.findByLabel(label);
    }

    private ArrayList<MegatopDTO> getMegatopDTOList(Collection<Megatop> megatopList) {
//        return megatopList.stream()
//                .filter(megatop -> "belwest.by".equals(megatop.getCompetitor()) ||
//                        (!megatop.getUrl().contains("/ru/") && !megatop.getUrl().contains("/kz/") &&
//                                !megatop.getDate().toLocalDate().isBefore(beforeDate)))
//                .map(MappingUtils::mapToMegatopDTO).distinct().collect(Collectors.toCollection(ArrayList::new));

        ArrayList<MegatopDTO> megatopDTOList = new ArrayList<>();
        Set<MegatopDTO> uniqueDTOs = new HashSet<>();  // Для хранения уникальных DTO

        for (Megatop megatop : megatopList) {
            if ("belwest.by".equals(megatop.getCompetitor()) ||
                    (!megatop.getUrl().contains("/ru/") && !megatop.getUrl().contains("/kz/") &&
                            !megatop.getDate().toLocalDate().isBefore(beforeDate))) {
                MegatopDTO megatopDTO = MappingUtils.mapToMegatopDTO(megatop);
                if (uniqueDTOs.add(megatopDTO)) {  // Добавить только уникальные DTO
                    megatopDTOList.add(megatopDTO);
                }
            }
        }

        return megatopDTOList;
    }

    private ArrayList<Megatop> getMegatopList(List<List<Object>> lists, String label, File file) {
        return lists.stream()
                .filter(str -> !"Категория 1".equals(str.get(0)))
                .map(str -> createMegatopFromList(str, Timestamp.from(Instant.now()), label, file)).distinct().collect(Collectors.toCollection(ArrayList::new));
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

    public List<String> getLatestLabels() {
        // Получаем последние 10 сохраненных меток из базы данных
        Pageable pageable = PageRequest.of(0, 10);
        return megatopRepository.findTop10DistinctLabels(pageable);
    }
}
