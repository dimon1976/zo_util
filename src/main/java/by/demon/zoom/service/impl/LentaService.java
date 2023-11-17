package by.demon.zoom.service.impl;


import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.lenta.LentaDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.ExcelUtil;
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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static by.demon.zoom.util.DateUtils.convertToLocalDateViaInstant;
import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class LentaService implements FileProcessingService {

    private HashMap<String, Lenta> data = new HashMap<>();
    private static final DateTimeFormatter LENTA_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Logger LOG = LoggerFactory.getLogger(LentaService.class);

    @Value("${out.path}")
    private String outPath;
    private int countSheet = 0;
    private final List<String> headerLentaTask = Arrays.asList("Id", "Наименование товара", "Вес", "Цена", "Москва, Нижний новгород", "Ростов-на-Дону", "Санкт-Петербург, Петрозаводск", "Новосибирск, Иркутск, Красноярск", "Екатеринбург", "Саратов, Уфа, Ульяновск", "Штрихкод");
    private final List<String> headerLentaReport = Arrays.asList("Город", "Товар", "Наименование товара", "Цена", "Сеть", "Акц. Цена 1", "Дата начала промо", "Дата окончания промо", "% скидки", "Механика акции", "Фото (ссылка)", "Доп.цена", "Модель", "Вес Едадил", "Вес Едадил, кг", "Вес Ленты", "Вес Ленты, кг", "Цена Едадил за КГ", "Пересчет к весу Ленты", "Доп. поле");


    public String exportReport(String filePath, File file, HttpServletResponse response, Date date) {
        try {
            LOG.info("Exporting report...");

            List<List<Object>> list = readExcel(file);
            Collection<Lenta> lentaList = getResultList(list);
            LocalDate afterDate = convertToLocalDateViaInstant(date);
            Collection<LentaReportDTO> lentaReportDTO = getLentaReportDTOList(lentaList, afterDate);
            try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
                ExcelUtil<LentaReportDTO> excelUtil = new ExcelUtil<>();
                short skip = 1;
                excelUtil.exportToExcel(headerLentaReport, lentaReportDTO, out, skip);
                excelUtil.download(file.getName(), filePath, response);
            }
            LOG.info("Report exported successfully: {}", filePath);
            return filePath;
        } catch (IOException e) {
            LOG.error("Error exporting report: {}", e.getMessage());
            return "Error exporting report";
        }
    }

    private Collection<Lenta> getResultList(List<List<Object>> list) {
        try {
            LOG.info("Getting result list...");

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
            LOG.info("Result list obtained successfully");
            return resultList;
        } catch (Exception e) {
            LOG.error("Error getting result list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private HashSet<LentaReportDTO> getLentaReportDTOList(Collection<Lenta> lentaList, LocalDate afterDate) {
        try {
            LOG.info("Getting LentaReportDTO list...");
            HashSet<LentaReportDTO> lentaReportDTOs = new HashSet<>();
            for (Lenta lenta : lentaList) {
                if (!lenta.getDateToPromo().isEmpty()) {
                    if (DateUtils.getDate(lenta.getDateToPromo(), LENTA_PATTERN).isAfter(afterDate)) {
                        LentaReportDTO lentaReportDTO = MappingUtils.mapToLentaReportDTO(lenta);
                        lentaReportDTOs.add(lentaReportDTO);
                    }
                }
            }
            LOG.info("LentaReportDTO list obtained successfully");
            return lentaReportDTOs;
        } catch (Exception e) {
            LOG.error("Error getting LentaReportDTO list: {}", e.getMessage());
            return new HashSet<>();
        }

    }

    // Обработка задания от ленты
    public String export(String filePath, File fileName, HttpServletResponse response, String... additionalParams) throws IOException {
        LOG.info("Exporting data...");
        try (Workbook workbook = loadWorkbook(fileName)) {
            for (Sheet sheet : workbook) {
                processSheet(sheet);
                countSheet++;
            }
            countSheet = 0;
            try {
                File file = Path.of(outPath, fileName.getName().toLowerCase(Locale.ROOT).replace("." + getExtension(fileName), ".xlsx")).toFile();
                LOG.info("Writing data...");

                Collection<LentaDTO> lentaDTOs = getLentaDTOList();
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    ExcelUtil<LentaDTO> excelUtil = new ExcelUtil<>();
                    excelUtil.exportToExcel(headerLentaTask, lentaDTOs, fileOutputStream, (short) 0);
                    excelUtil.download(file.getName(), filePath, response);
                }
                data = new HashMap<>();

                LOG.info("Data written successfully");
                return filePath;
            } catch (IOException e) {
                LOG.error("Error exporting data: {}", e.getMessage());
                return "Error exporting data";
            }
        }
    }


    private Workbook loadWorkbook(File filename) {
        LOG.info("Loading workbook...");
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
            LOG.error("Error loading workbook: {}", e.getMessage());
            throw new RuntimeException("Error loading workbook", e);
        }

    }

    @NotNull
    private String getExtension(File filename) {
        LOG.info("Getting file extension...");
        return filename.getName().substring(filename.getName().lastIndexOf(".") + 1).toLowerCase();
    }

    // Обработка листа

    private void processSheet(Sheet sheet) {
        try {
            LOG.info("Processing sheet...");

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
                ExcelUtil.getRowList(row, linked);
                if (countSheet == 0) {
                    data.put(linked.get(0).toString(), new Lenta());
                }
                addLenta(linked, data.get(linked.get(0).toString()));
            }
            LOG.info("Sheet processed successfully");
        } catch (Exception e) {
            LOG.error("Error processing sheet: {}", e.getMessage());
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
            LOG.info("Lenta added successfully");
        } catch (Exception e) {
            LOG.error("Error adding Lenta: {}", e.getMessage());
        }
    }

    private Collection<LentaDTO> getLentaDTOList() {
        try {
            LOG.info("Getting LentaDTO list...");

            Collection<LentaDTO> lentaDTOS = new ArrayList<>();
            for (Map.Entry<String, Lenta> lenta : data.entrySet()) {
                LentaDTO lentaDTO = MappingUtils.mapToLentaDTO(lenta.getValue());
                lentaDTOS.add(lentaDTO);
            }
            LOG.info("LentaDTO list obtained successfully");
            return lentaDTOS;
        } catch (Exception e) {
            LOG.error("Error getting LentaDTO list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String saveAll(String filePath, File transferTo, HttpServletResponse response, String... additionalParams) {
        return null;
    }

    @Override
    public String deleteAll() {
        return null;
    }
}

