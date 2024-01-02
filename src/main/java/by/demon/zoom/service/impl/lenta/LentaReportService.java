package by.demon.zoom.service.impl.lenta;


import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.DateUtils.convertToLocalDateViaInstant;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class LentaReportService implements FileProcessingService<LentaReportDTO> {


    private static final DateTimeFormatter LENTA_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Logger log = LoggerFactory.getLogger(LentaReportService.class);

    @Value("${out.path}")
    private String outPath;

    private final DataDownload dataDownload;
    private final DataToExcel<LentaReportDTO> dataToExcel;

    private final List<String> header = Arrays.asList("Город", "Товар", "Наименование товара", "Цена", "Сеть", "Акц. Цена 1", "Дата начала промо", "Дата окончания промо", "% скидки", "Механика акции", "Фото (ссылка)", "Доп.цена", "Модель", "Вес Едадил", "Вес Едадил, кг", "Вес Ленты", "Вес Ленты, кг", "Цена Едадил за КГ", "Пересчет к весу Ленты", "Доп. поле");

    public LentaReportService(DataDownload dataDownload, DataToExcel<LentaReportDTO> dataToExcel) {
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<LentaReportDTO> readFiles(List<File> files, String... additionalParams) {
        ArrayList<LentaReportDTO> allUrlDTOs = new ArrayList<>(); // Создаем переменную для сохранения всех DTO
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String var = additionalParams[1];
        for (File file : files) {
            try {
                log.info("Exporting report...");
                Date date = formatter.parse(var);
                List<List<Object>> list = readDataFromFile(file);
                Files.delete(file.toPath());
                Collection<Lenta> lentaList = getResultList(list);
                LocalDate afterDate = convertToLocalDateViaInstant(date);
                Collection<LentaReportDTO> lentaReportDTO = getLentaReportDTOList(lentaList, afterDate);
                allUrlDTOs.addAll(lentaReportDTO);
                log.info("File {} successfully read", file.getName());

            } catch (Exception e) {
                log.error("Error processing file: {}", file.getAbsolutePath(), e);

            } finally {
                if (file.exists()) {
                    if (!file.delete()) {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        return allUrlDTOs;
    }

    public void download(ArrayList<LentaReportDTO> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = DataDownload.getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportToExcel(header, list, out, 1);
                        Files.write(path, out.toByteArray());
                    }
                    dataDownload.downloadExcel(path, response);
                    DataDownload.cleanupTempFile(path);
                    break;
                case "csv":
                    List<String> strings = convert(list);
                    dataDownload.downloadCsv(path, strings, header, response);
                    break;
                default:
                    log.error("Incorrect format: {}", format);
                    break;
            }

            log.info("Data exported successfully to {}: {}", format, path.getFileName().toString());
        } catch (IOException e) {
            log.error("Error exporting data to {}: {}", format, e.getMessage(), e);
            throw e;
        }
    }

    private static List<String> convert(List<LentaReportDTO> objectList) {
        return objectList.stream()
                .filter(Objects::nonNull)
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }

    @Override
    public String save(ArrayList<LentaReportDTO> collection) {
        return null;
    }

    private Collection<Lenta> getResultList(List<List<Object>> list) {
        try {
            log.info("Getting result list...");

            ArrayList<Lenta> resultList = new ArrayList<>();
            int count = 0;
            for (List<Object> str : list) {
                if (count < 1) {
                    count++;
                    continue;
                }
                Lenta lenta = new Lenta();
                lenta.setCity(String.valueOf(str.get(0)));
                lenta.setProduct(StringUtil.cleanAndReplace(String.valueOf(str.get(1)), "."));
                lenta.setProductName(String.valueOf(str.get(2)));
                lenta.setPrice(StringUtil.cleanAndReplace(String.valueOf(str.get(3)), "."));
                lenta.setNetwork(String.valueOf(str.get(4)));
                lenta.setActionPrice1(StringUtil.cleanAndReplace(String.valueOf(str.get(5)), "."));
                lenta.setDateFromPromo(String.valueOf(str.get(6)));
                lenta.setDateToPromo(String.valueOf(str.get(7)));
                lenta.setDiscountPercentage(StringUtil.cleanAndReplace(String.valueOf(str.get(8)), "."));
                lenta.setMechanicsOfTheAction(String.valueOf(str.get(9)));
                lenta.setUrl(String.valueOf(str.get(10)));
                lenta.setAdditionalPrice(String.valueOf(str.get(11)));
                lenta.setModel(String.valueOf(str.get(12)));
                lenta.setWeightEdeadeal(String.valueOf(str.get(13)));
                lenta.setWeightEdeadealKg(String.valueOf(str.get(14)));
                lenta.setWeightLenta(StringUtil.cleanAndReplace(String.valueOf(str.get(15)), "."));
                lenta.setWeightLentaKg(String.valueOf(str.get(16)));
                lenta.setPriceEdeadealKg(StringUtil.cleanAndReplace(String.valueOf(str.get(17)), "."));
                lenta.setConversionToLentaWeight(StringUtil.cleanAndReplace(String.valueOf(str.get(18)), "."));
                lenta.setAdditionalField(String.valueOf(str.get(22)));
                resultList.add(lenta);
                count++;
            }
            log.info("Result list obtained successfully");
            return resultList;
        } catch (Exception e) {
            log.error("Error getting result list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private HashSet<LentaReportDTO> getLentaReportDTOList(Collection<Lenta> lentaList, LocalDate afterDate) {
        try {
            log.info("Getting LentaReportDTO list...");
            HashSet<LentaReportDTO> lentaReportDTOs = new HashSet<>();
            for (Lenta lenta : lentaList) {
                if (!lenta.getDateToPromo().isEmpty()) {
                    if (DateUtils.getLocalDate(lenta.getDateToPromo(), LENTA_PATTERN).isAfter(afterDate)) {
                        LentaReportDTO lentaReportDTO = MappingUtils.mapToLentaReportDTO(lenta);
                        lentaReportDTOs.add(lentaReportDTO);
                    }
                }
            }
            log.info("LentaReportDTO list obtained successfully");
            return lentaReportDTOs;
        } catch (Exception e) {
            log.error("Error getting LentaReportDTO list: {}", e.getMessage());
            return new HashSet<>();
        }

    }


}

