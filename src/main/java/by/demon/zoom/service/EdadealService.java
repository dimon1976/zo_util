package by.demon.zoom.service;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.dto.lenta.LentaDTO;
import by.demon.zoom.util.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

@Service
public class EdadealService {

    @Value("${out.path}")
    private String outPath;
    private HashMap<String, Lenta> data = new HashMap<>();
    private int countSheet = 0;
    private final List<String> header = Arrays.asList("Категория из файла", "Сайт", "ZMS ID", "Категория", "Бренд", "Модель", "Код производителя", "Цена", "Маркетинговое описание", "Маркетинговое описание 3",
            "Маркетинговое описание 4", "Статус", "Ссылка", "Старая цена", "Продавец", "Дата", "Позиция", "Ссылка на родителя");

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

    private void write(String filePath, File fileName, HttpServletResponse response) throws IOException {
        File file = Path.of(outPath, fileName.getName().toLowerCase(Locale.ROOT).replace("." + getExtension(fileName), ".xlsx")).toFile();
        Collection<LentaDTO> lentaDTOs = getLentaDTOList();
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            ExcelUtil<LentaDTO> excelUtil = new ExcelUtil<>();
            excelUtil.exportExcel(header, lentaDTOs, fileOutputStream, (short) 1);
            excelUtil.download(file.getName(), filePath, response);
        }
        data = new HashMap<>();
    }
}
