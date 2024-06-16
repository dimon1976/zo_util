package by.demon.zoom.service.impl.lenta;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.DateUtils.convertToLocalDateViaInstant;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileUtil.downloadFile;
import static by.demon.zoom.util.FileUtil.getPath;

@Service
public class LentaReportService implements FileProcessingService<LentaReportDTO> {

    private static final Logger log = LoggerFactory.getLogger(LentaReportService.class);
    private final List<String> header = Arrays.asList("Город", "Товар", "Наименование товара", "Цена", "Сеть", "Акц. Цена 1", "Дата начала промо", "Дата окончания промо", "% скидки", "Механика акции", "Фото (ссылка)", "Доп.цена", "Модель", "Вес Едадил", "Вес Едадил, кг", "Вес Ленты", "Вес Ленты, кг", "Цена Едадил за КГ", "Пересчет к весу Ленты", "Доп. поле");
    private static final DateTimeFormatter LENTA_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final DataToExcel<LentaReportDTO> dataToExcel;

    public LentaReportService(DataToExcel<LentaReportDTO> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<LentaReportDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<LentaReportDTO> allUrlDTOs = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String var = additionalParams[1];
        List<String> errorMessages = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<LentaReportDTO>>> futures = files.stream()
                .map(file -> executorService.<ArrayList<LentaReportDTO>>submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        Date date = formatter.parse(var);
                        List<List<Object>> list = readDataFromFile(file);
                        Files.delete(file.toPath());
                        Collection<Lenta> lentaList = getResultList(list);
                        LocalDate afterDate = convertToLocalDateViaInstant(date);
                        Collection<LentaReportDTO> lentaReportDTO = getLentaReportDTOList(lentaList, afterDate);
                        log.info("File {} successfully read", file.getName());
                        return new ArrayList<>(lentaReportDTO);
                    } catch (Exception e) {
                        log.error("Error processing file: {}", file.getAbsolutePath(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                        return new ArrayList<>();
                    }
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<LentaReportDTO>> future : futures) {
            try {
                allUrlDTOs.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }

        return allUrlDTOs;
    }


    public void download(ArrayList<LentaReportDTO> list, HttpServletResponse response, String format) throws IOException {
        String fileName = "lenta_report_data";
        Path path = getPath(fileName, format.equals("excel") ? ".xlsx" : ".csv");
        downloadFile(header, list, response, format, path, dataToExcel);
    }

    @Override
    public String save(ArrayList<LentaReportDTO> collection) {
        return null;
    }

    private Collection<Lenta> getResultList(List<List<Object>> list) {
        try {
            log.info("Getting result list...");

            return list.stream()
                    .skip(1) // Пропуск заголовка
                    .map(this::createLentaFromList)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting result list: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Lenta createLentaFromList(List<Object> str) {
        Lenta lenta = new Lenta();
        lenta.setCity(getStringValue(str, 0));
        lenta.setProduct(StringUtil.cleanAndReplace(getStringValue(str, 1), "."));
        lenta.setProductName(getStringValue(str, 2));
        lenta.setPrice(StringUtil.cleanAndReplace(getStringValue(str, 3), "."));
        lenta.setNetwork(getStringValue(str, 4));
        lenta.setActionPrice1(StringUtil.cleanAndReplace(getStringValue(str, 5), "."));
        lenta.setDateFromPromo(getStringValue(str, 6));
        lenta.setDateToPromo(getStringValue(str, 7));
        lenta.setDiscountPercentage(StringUtil.cleanAndReplace(getStringValue(str, 8), "."));
        lenta.setMechanicsOfTheAction(getStringValue(str, 9));
        lenta.setUrl(getStringValue(str, 10));
        lenta.setAdditionalPrice(getStringValue(str, 11));
        lenta.setModel(getStringValue(str, 12));
        lenta.setWeightEdeadeal(getStringValue(str, 13));
        lenta.setWeightEdeadealKg(getStringValue(str, 14));
        lenta.setWeightLenta(StringUtil.cleanAndReplace(getStringValue(str, 15), "."));
        lenta.setWeightLentaKg(getStringValue(str, 16));
        lenta.setPriceEdeadealKg(StringUtil.cleanAndReplace(getStringValue(str, 17), "."));
        lenta.setConversionToLentaWeight(StringUtil.cleanAndReplace(getStringValue(str, 18), "."));
        lenta.setAdditionalField(getStringValue(str, 19));
        return lenta;
    }

    private HashSet<LentaReportDTO> getLentaReportDTOList(Collection<Lenta> lentaList, LocalDate afterDate) {
        try {
            log.info("Getting LentaReportDTO list...");
            return lentaList.stream()
                    .filter(lenta -> !lenta.getDateToPromo().isEmpty() && DateUtils.getLocalDate(lenta.getDateToPromo(), LENTA_PATTERN).isAfter(afterDate))
                    .map(MappingUtils::mapToLentaReportDTO)
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (Exception e) {
            log.error("Error getting LentaReportDTO list: {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }
}
