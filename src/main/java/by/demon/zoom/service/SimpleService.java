package by.demon.zoom.service;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);
        Collection<Product> productList = getProductList(lists);
        Collection<SimpleDTO> collect = getSimpleDTOList(productList);
        try (OutputStream out = Files.newOutputStream(of)) {
            short skipLines = 1;
            excelUtil.exportExcel(header, collect, out, skipLines);
            excelUtil.download(file.getName(), filePath, response);
        }
        return filePath;
    }

    private Collection<SimpleDTO> getSimpleDTOList(Collection<Product> simpleArrayList) {
        List<SimpleDTO> list = new ArrayList<>();
        for (Product product : simpleArrayList) {
            SimpleDTO simpleDTO = MappingUtils.mapToSimpleDTO(product);
            list.add(simpleDTO);
        }
        return list;
    }

    private Collection<Product> getProductList(List<List<Object>> lists) {
        ArrayList<Product> list = new ArrayList<>();
        for (List<Object> str : lists) {
            Product product = new Product();
            product.setId(String.valueOf(str.get(0)));
            product.setCategory1(String.valueOf(str.get(1)));
            product.setCategory2(String.valueOf(str.get(2)));
            product.setCategory3(String.valueOf(str.get(3)));
            product.setBrand(String.valueOf(str.get(4)));
            product.setModel(String.valueOf(str.get(5)));
            product.setProduct(String.valueOf(str.get(6)));//
            product.setCity(String.valueOf(str.get(7)));
            product.setCompetitor(String.valueOf(str.get(8)));
            product.setTime(String.valueOf(str.get(9)));
            product.setDate(String.valueOf(str.get(10)));
            product.setCompetitorPrice(String.valueOf(str.get(11)));
            product.setCompetitorOldPrice(String.valueOf(str.get(12)));
            product.setCompetitorActionPrice(String.valueOf(str.get(13)));
            product.setComment(String.valueOf(str.get(14)));
            product.setCompetitorModel(String.valueOf(str.get(15)));
            product.setYearCompetitor(String.valueOf(str.get(16)));
            product.setAnalogue(String.valueOf(str.get(17)));
            product.setAddressOfTheCompetitor(String.valueOf(str.get(18)));
            product.setStatus(String.valueOf(str.get(19)));
            product.setPromo(String.valueOf(str.get(20)));
            product.setUrl(String.valueOf(str.get(21)));
            product.setCompetitorUrl(String.valueOf(str.get(22)));
            product.setWebCacheUrl(String.valueOf(str.get(23)));
            list.add(product);
        }
        return list;
    }

}
