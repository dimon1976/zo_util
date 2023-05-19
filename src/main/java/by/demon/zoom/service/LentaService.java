package by.demon.zoom.service;


import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.lenta.LentaDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.DateUtils;
import by.demon.zoom.util.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static by.demon.zoom.util.ExcelUtil.*;

@Service
public class LentaService {

    private HashMap<String, Lenta> data = new HashMap<>();
    private static final DateTimeFormatter LENTA_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final LocalDate afterDate = LocalDate.of(2023, 4, 16);
    @Value("${out.path}")
    private String outPath;
    private int countSheet = 0;
    private final String[] headerLentaTask = {"Id", "Наименование товара", "Вес", "Цена", "Москва, Нижний новгород", "Ростов-на-Дону", "Санкт-Петербург, Петрозаводск", "Новосибирск, Иркутск, Красноярск", "Екатеринбург", "Саратов, Уфа, Ульяновск", "Штрихкод"};
    private final String[] headerLentaReport = {"Город", "Товар", "Наименование товара", "Цена", "Сеть", "Акц. Цена 1", "Дата начала промо", "Дата окончания промо", "% скидки", "Механика акции", "Фото (ссылка)", "Доп.цена", "Модель", "Вес Едадил", "Вес Едадил, кг", "Вес Ленты", "Вес Ленты, кг", "Цена Едадил за КГ", "Пересчет к весу Ленты"};


    public String exportReport(String filePath, File file, HttpServletResponse response) throws IOException {
        List<List<Object>> list = readExcel(file);
        Collection<Lenta> lentaList = getResultList(list);
        Collection<LentaReportDTO> lentaReportDTO = getLentaReportDTOList(lentaList);
        try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
            ExcelUtil<LentaReportDTO> excelUtil = new ExcelUtil<>();
            short skip = 1;
            excelUtil.exportExcel(headerLentaReport, lentaReportDTO, out, skip);
            excelUtil.download(file.getName(), filePath, response);
        }
        return filePath;
    }

    private Collection<Lenta> getResultList(List<List<Object>> list) {
        DecimalFormat nf = new DecimalFormat("0.00");
        ArrayList<Lenta> resultList = new ArrayList<>();
        int count = 0;
        for (List<Object> str : list) {
            if (count < 1) {
                count++;
                continue;
            }
            Lenta lenta = new Lenta();
            lenta.setCity(String.valueOf(str.get(0)));
            lenta.setProduct(String.valueOf(str.get(1)));
            lenta.setProductName(String.valueOf(str.get(2)));
            lenta.setPrice(String.valueOf(str.get(3)));
            lenta.setNetwork(String.valueOf(str.get(4)));
            lenta.setActionPrice1(String.valueOf(str.get(5)));
            lenta.setDateFromPromo(String.valueOf(str.get(6)));
            lenta.setDateToPromo(String.valueOf(str.get(7)));
            lenta.setDiscountPercentage(String.valueOf(str.get(8)));
            lenta.setMechanicsOfTheAction(String.valueOf(str.get(9)));
            lenta.setUrl(String.valueOf(str.get(10)));
            lenta.setAdditionalPrice(String.valueOf(str.get(11)));
            lenta.setModel(String.valueOf(str.get(12)));
            lenta.setWeightEdeadeal(String.valueOf(str.get(13)));
            lenta.setWeightEdeadealKg(String.valueOf(str.get(14)));
            lenta.setWeightLenta(String.valueOf(str.get(15)));
            lenta.setWeightLentaKg(String.valueOf(str.get(16)));
            lenta.setPriceEdeadealKg(getFormattedString(String.valueOf(str.get(17)), nf));
            lenta.setConversionToLentaWeight(getFormattedString(String.valueOf(str.get(18)), nf));
            resultList.add(lenta);
            count++;
        }
        return resultList;
    }

    private String getFormattedString(String value, DecimalFormat pattern) {
        if (value.equals("")) {
            return value;
        }
        Float i = Float.parseFloat(value);
        return pattern.format(i);
    }

    private HashSet<LentaReportDTO> getLentaReportDTOList(Collection<Lenta> lentaList) {
        HashSet<LentaReportDTO> lentaReportDTOs = new HashSet<>();
        for (Lenta lenta : lentaList) {
            if (!lenta.getDateToPromo().equals("")) {
                if (DateUtils.getDate(lenta.getDateToPromo(), LENTA_PATTERN).isAfter(afterDate)) {
                    LentaReportDTO lentaReportDTO = MappingUtils.mapToLentaReportDTO(lenta);
                    lentaReportDTOs.add(lentaReportDTO);
                }
            }
        }
        return lentaReportDTOs;
    }


    public String export(String filePath, File filename, HttpServletResponse response) throws IOException {
        try (Workbook workbook = loadWorkbook(filename)) {
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                processSheet(sheet);
                countSheet++;
            }
            countSheet = 0;
            workbook.close();
            write(filePath, filename, response);
        }
        return filePath;
    }

    private Workbook loadWorkbook(File filename) throws IOException {
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
        }

    }

    @NotNull
    private String getExtension(File filename) {
        return filename.getName().substring(filename.getName().lastIndexOf(".") + 1).toLowerCase();
    }

    // Обработка листа
    private void processSheet(Sheet sheet) {
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
    }


    // Обработка строки
    private void addLenta(List<Object> row, Lenta lenta) {
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
    }

    private String isNull(Cell cell) {
        if (cell == null) {
            return "";
        } else {
            return cell.toString();
        }
    }

    private void write(String filePath, File fileName, HttpServletResponse response) throws IOException {
        File file = Path.of(outPath, fileName.getName().toLowerCase(Locale.ROOT).replace("." + getExtension(fileName), ".xlsx")).toFile();
        Collection<LentaDTO> lentaDTOs = getLentaDTOList();
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            ExcelUtil<LentaDTO> excelUtil = new ExcelUtil<>();
            excelUtil.exportExcel(headerLentaTask, lentaDTOs, fileOutputStream, (short) 1);
            excelUtil.download(file.getName(), filePath, response);
        }
        data = new HashMap<>();
    }


    private Collection<LentaDTO> getLentaDTOList() {
        Collection<LentaDTO> lentaDTOS = new ArrayList<>();
        for (Map.Entry<String, Lenta> lenta : data.entrySet()) {
            LentaDTO lentaDTO = MappingUtils.mapToLentaDTO(lenta.getValue());
            lentaDTOS.add(lentaDTO);
        }
        return lentaDTOS;
    }
}
