package by.demon.zoom.service;

import by.demon.zoom.domain.Simple;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class SimpleService {

    private final ExcelUtil<SimpleDTO> excelUtil;

    private final String[] header = {"ID", "Категория 1", "Категория 2", "Категория 3", "Бренд", "Модель", "Цена Simplewine на дату парсинга", "Город", "Конкурент", "Время выкачки"
            , "Дата", "Цена конкурента регуляр (по карте)", "Цена конкурента без карты", "Цена конкурента промо/акция"
            , "Комментарий", "Наименование товара конкурента", "Год конкурента", "Аналог", "Адрес конкурента"
            , "Статус товара (В наличии/Под заказ/Нет в наличии)", "Промо (да/нет)", "Ссылка конкурент", "Ссылка Симпл", "Скриншот"};

    public SimpleService(ExcelUtil<SimpleDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }

    public String export(String filePath, File file, HttpServletResponse response) throws IOException {
        String fileName = file.getName();
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);
        Collection<Simple> simpleArrayList = getSimpleList(lists);
        Collection<SimpleDTO> collect = getSimpleDTOList(simpleArrayList);
        try (OutputStream out = Files.newOutputStream(of)) {
            short skipLines = 1;
            excelUtil.exportExcel(header, collect, out, skipLines);
            excelUtil.download(file.getName(), filePath, response);
        }
        return filePath;


    }

    private Collection<SimpleDTO> getSimpleDTOList(Collection<Simple> simpleArrayList) {
        return null;
    }

    private Collection<Simple> getSimpleList(List<List<Object>> lists) {
        return null;
    }

}
