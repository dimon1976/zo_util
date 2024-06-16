package by.demon.zoom.service.impl.lenta;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.dto.lenta.LentaTaskDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelReader.processRow;
import static by.demon.zoom.util.FileUtil.*;


@Service
public class LentaTaskService implements FileProcessingService<LentaTaskDTO> {


    private HashMap<String, Lenta> data = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(LentaTaskService.class);
    private final DataToExcel<LentaTaskDTO> dataToExcel;
    private final List<String> header = Arrays.asList("Id", "Наименование товара", "Вес", "Цена", "Москва, Нижний новгород", "Ростов-на-Дону",
            "Санкт-Петербург, Петрозаводск", "Новосибирск, Иркутск, Красноярск", "Екатеринбург", "Саратов, Уфа, Ульяновск", "Штрихкод");
    private int countSheet = 0;

    public LentaTaskService(DataToExcel<LentaTaskDTO> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }


    @Override
    // Обработка задания от ленты
    public ArrayList<LentaTaskDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<LentaTaskDTO> allUrlDTOs = new ArrayList<>();
        log.info("Exporting data...");
        for (File file : files) {
            try (Workbook workbook = loadWorkbook(file)) {
                for (Sheet sheet : workbook) {
                    processSheet(sheet);
                    countSheet++;
                }
                countSheet = 0;
                Collection<LentaTaskDTO> lentaDTOs = getLentaDTOList();
                allUrlDTOs.addAll(lentaDTOs);
                data = new HashMap<>();
            }
        }
        return allUrlDTOs;
    }

    public void download(ArrayList<LentaTaskDTO> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportToExcel(header, list, out, 0);
                        Files.write(path, out.toByteArray());
                    }
                    downloadExcel(path, response);
                    cleanupTempFile(path);
                    break;
                case "csv":
                    List<String> strings = convert(list);
                    downloadCsv(path, strings, header, response);
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

    private static List<String> convert(List<LentaTaskDTO> objectList) {
        return objectList.stream()
                .filter(Objects::nonNull)
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }

    @Override
    public String save(ArrayList<LentaTaskDTO> collection) {
        return null;
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
        } catch (Exception e) {
            log.error("Error adding Lenta: {}", e.getMessage());
        }
    }

    private Collection<LentaTaskDTO> getLentaDTOList() {
        try {
            log.info("Getting LentaDTO list...");

            Collection<LentaTaskDTO> lentaDTOS = new ArrayList<>();
            for (Map.Entry<String, Lenta> lenta : data.entrySet()) {
                LentaTaskDTO lentaDTO = MappingUtils.mapToLentaDTO(lenta.getValue());
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
