package by.demon.zoom.service.impl;


import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.lenta.LentaDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static by.demon.zoom.util.DateUtils.convertToLocalDateViaInstant;
import static by.demon.zoom.util.ExcelReader.processRow;
import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class LentaService<T> implements FileProcessingService<T> {

    private HashMap<String, Lenta> data = new HashMap<>();
    private static final DateTimeFormatter LENTA_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Logger log = LoggerFactory.getLogger(LentaService.class);

    @Value("${out.path}")
    private String outPath;
    private int countSheet = 0;
    private final DataDownload dataDownload;
    private final List<String> headerLentaTask = Arrays.asList("Id", "Наименование товара", "Вес", "Цена", "Москва, Нижний новгород", "Ростов-на-Дону", "Санкт-Петербург, Петрозаводск", "Новосибирск, Иркутск, Красноярск", "Екатеринбург", "Саратов, Уфа, Ульяновск", "Штрихкод");
    private final List<String> headerLentaReport = Arrays.asList("Город", "Товар", "Наименование товара", "Цена", "Сеть", "Акц. Цена 1", "Дата начала промо", "Дата окончания промо", "% скидки", "Механика акции", "Фото (ссылка)", "Доп.цена", "Модель", "Вес Едадил", "Вес Едадил, кг", "Вес Ленты", "Вес Ленты, кг", "Цена Едадил за КГ", "Пересчет к весу Ленты", "Доп. поле");

    public LentaService(DataDownload dataDownload) {
        this.dataDownload = dataDownload;
    }


    public String exportReport(String filePath, File file, HttpServletResponse response, Date date) {
        try {
            log.info("Exporting report...");

            List<List<Object>> list = readDataFromFile(file);
            Collection<Lenta> lentaList = getResultList(list);
            LocalDate afterDate = convertToLocalDateViaInstant(date);
            Collection<LentaReportDTO> lentaReportDTO = getLentaReportDTOList(lentaList, afterDate);
            try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
                DataToExcel<LentaReportDTO> dataToExcel = new DataToExcel<>();
                short skip = 1;
                dataToExcel.exportToExcel(headerLentaReport, lentaReportDTO, out, skip);
//                dataDownload.download(file.getName(), filePath, response);
            }
            log.info("Report exported successfully: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Error exporting report: {}", e.getMessage());
            return "Error exporting report";
        }
    }

    @Override
    public Collection<?> readFiles(List<File> files, String... additionalParams) throws IOException {
        switch (additionalParams[0]) {
            case "report":
                return readReportFiles(files, additionalParams);
            case "task":
                return readTaskFile(files);
            case "edadeal":
                break;

            default:
                log.info("Error action {}", additionalParams[0]);
                break;
        }
        return null;
    }

    public Collection<LentaReportDTO> readReportFiles(List<File> files, String... additionalParams) {
        Collection<LentaReportDTO> allUrlDTOs = new ArrayList<>(); // Создаем переменную для сохранения всех DTO
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String var = additionalParams[1];
        for (File file : files) {
            try {
                log.info("Exporting report...");
                Date date = formatter.parse(var);
                List<List<Object>> list = readDataFromFile(file);
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

    // Обработка задания от ленты
    public Collection<LentaDTO> readTaskFile(List<File> files) throws IOException {
        Collection<LentaDTO> allUrlDTOs = new ArrayList<>();
        log.info("Exporting data...");
        for (File file : files) {
            try (Workbook workbook = loadWorkbook(file)) {
                for (Sheet sheet : workbook) {
                    processSheet(sheet);
                    countSheet++;
                }
                countSheet = 0;
                Collection<LentaDTO> lentaDTOs = getLentaDTOList();
                allUrlDTOs.addAll(lentaDTOs);
                data = new HashMap<>();
            }
        }
        return allUrlDTOs;
    }

    private Workbook loadWorkbook(File filename) {
        log.info("Loading workbook...");
        String extension = getExtension(filename);
        try (FileInputStream file = new FileInputStream(filename)) {
            switch (extension) {
                case "xls":
                    // old format
                    return new HSSFWorkbook(file);
                case "xlsx":
                    // new format
                    return new XSSFWorkbook(file);
                default:
                    throw new RuntimeException("Unknown Excel file extension: " + extension);
            }
        } catch (IOException e) {
            log.error("Error loading workbook: {}", e.getMessage());
            throw new RuntimeException("Error loading workbook", e);
        }

    }

    @NotNull
    private String getExtension(File filename) {
        log.info("Getting file extension...");
        return filename.getName().substring(filename.getName().lastIndexOf(".") + 1).toLowerCase();
    }

    // Обработка листа

    private void processSheet(Sheet sheet) {
        try {
            log.info("Processing sheet...");

            Row row;
            int counter = 0;
            for (int i = sheet.getFirstRowNum(); counter < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);
                if (row == null) {
                    continue;
                } else {
                    counter++;
                }
                List<Object> linked = new LinkedList<>();
                processRow(row, linked);
                if (countSheet == 0) {
                    data.put(linked.get(0).toString(), new Lenta());
                }
                addLenta(linked, data.get(linked.get(0).toString()));
            }
            log.info("Sheet processed successfully");
        } catch (Exception e) {
            log.error("Error processing sheet: {}", e.getMessage());
        }
    }

    // Обработка строки

    private void addLenta(List<Object> row, Lenta lenta) {
        try {
            if (row != null) {
                if (countSheet == 0) {
                    lenta.setId(row.get(0).toString());
                    lenta.setModel(row.get(1).toString());
                    lenta.setPrice(row.get(2).toString());
                    lenta.setMoscow(row.get(3).toString());
                    lenta.setRostovNaDonu(row.get(4).toString());
                    lenta.setSpb(row.get(5).toString());
                    lenta.setNovosibirsk(row.get(6).toString());
                    lenta.setYekaterinburg(row.get(7).toString());
                    lenta.setSaratov(row.get(8).toString());
                }
            }
            if (countSheet == 1 && lenta != null) {
                assert row != null;
                lenta.getEan().add(row.get(2).toString());
            }
            if (countSheet == 2 && lenta != null) {
                assert row != null;
                lenta.setWeight(row.get(2).toString());
            }
            log.info("Lenta added successfully");
        } catch (Exception e) {
            log.error("Error adding Lenta: {}", e.getMessage());
        }
    }

    private Collection<LentaDTO> getLentaDTOList() {
        try {
            log.info("Getting LentaDTO list...");

            Collection<LentaDTO> lentaDTOS = new ArrayList<>();
            for (Map.Entry<String, Lenta> lenta : data.entrySet()) {
                LentaDTO lentaDTO = MappingUtils.mapToLentaDTO(lenta.getValue());
                lentaDTOS.add(lentaDTO);
            }
            log.info("LentaDTO list obtained successfully");
            return lentaDTOS;
        } catch (Exception e) {
            log.error("Error getting LentaDTO list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}

