package by.demon.zoom.service;


import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.LentaDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static by.demon.zoom.util.ExcelUtil.getValue;
import static by.demon.zoom.util.ExcelUtil.getValueFormula;

@Service
public class LentaService {

    private final ExcelUtil<LentaDTO> excelUtil;
    private HashMap<String, Lenta> data = new HashMap<>();
    @Value("${out.path}")
    private String outPath;
    private int countSheet = 0;
    private final String[] header = {"Id", "Наименование товара", "Вес", "Цена", "Москва, Нижний новгород", "Ростов-на-Дону", "Санкт-Петербург, Петрозаводск", "Новосибирск, Иркутск, Красноярск", "Екатеринбург", "Саратов, Уфа, Ульяновск", "Штрихкод"};



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
            getRowList(row, linked);
            if (countSheet == 0) {
                data.put(linked.get(0).toString(), new Lenta());
            }
            addLenta(linked, data.get(linked.get(0).toString()));
        }
    }

    public static void getRowList(Row row, List<Object> linked) {
        Cell cell;
        Object value;
        for (int j = 0; j <= row.getLastCellNum(); j++) {
            cell = row.getCell(j);
            if (cell == null) {
                linked.add("");
                continue;
            }
            if (cell.getCellType() == CellType.FORMULA) {
                value = getValueFormula(cell);
                linked.add(value);
            } else {
                value = getValue(cell);
                linked.add(value);
            }
        }
    }


    // Обработка строки
    private void addLenta(List<Object> row, Lenta lenta) {
        if (row != null) {
            if (countSheet == 0) {
                lenta.setId(row.get(0).toString());
                lenta.setModel(row.get(1).toString());
//                lenta.setWeight(row.get(2).toString());
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


    private void write(String filePath, File fileName, HttpServletResponse response) throws IOException {
        File file = Path.of(outPath, fileName.getName().toLowerCase(Locale.ROOT).replace("." + getExtension(fileName), ".xlsx")).toFile();
        Collection<LentaDTO> megatopArrayList = getLentaDTOList();
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            excelUtil.exportExcel(header, megatopArrayList, fileOutputStream, (short) 1);
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

    public LentaService(ExcelUtil<LentaDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }
}
