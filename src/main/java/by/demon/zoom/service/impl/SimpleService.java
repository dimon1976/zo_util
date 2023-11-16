package by.demon.zoom.service.impl;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.ExcelUtil;
import by.demon.zoom.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class SimpleService implements FileProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleService.class);
    private final ExcelUtil<SimpleDTO> excelUtil;

    private final List<String> header = Arrays.asList("ID", "Категория 1", "Категория 2", "Категория 3", "Бренд", "Модель", "Цена Simplewine на дату парсинга", "Город", "Конкурент", "Время выкачки"
            , "Дата", "Цена конкурента регуляр (по карте)", "Цена конкурента без карты", "Цена конкурента промо/акция"
            , "Комментарий", "Наименование товара конкурента", "Год конкурента", "Аналог", "Адрес конкурента"
            , "Статус товара (В наличии/Под заказ/Нет в наличии)", "Промо (да/нет)", "Ссылка конкурент", "Ссылка Симпл", "Скриншот");

    public SimpleService(ExcelUtil<SimpleDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }

    public String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);
        Collection<Product> productList = getProductList(lists);
        Collection<SimpleDTO> collect = getSimpleDTOList(productList);
        try (OutputStream out = Files.newOutputStream(of)) {
            short skipLines = 1;
            excelUtil.exportToWorkbookExcel(header, collect, out, skipLines);
            excelUtil.download(file.getName(), filePath, response);
            LOG.info("Data exported successfully to Excel: {}", filePath);
        } catch (IOException e) {
            LOG.error("Error exporting data to Excel: {}", e.getMessage(), e);
            throw e;
        }
        return filePath;
    }


    private Collection<SimpleDTO> getSimpleDTOList(Collection<Product> simpleArrayList) {
        return simpleArrayList.stream()
                .skip(1) //пропускаем заголовок
                .map(MappingUtils::mapToSimpleDTO)
                .collect(Collectors.toList());
    }

    private Collection<Product> getProductList(List<List<Object>> lists) {
        return lists.stream()
                .map(this::createProductFromList)
                .collect(Collectors.toList());
    }

    private Product createProductFromList(List<Object> str) {
        Product product = new Product();
        product.setId(getStringValue(str, 0));
        product.setCategory1(getStringValue(str, 1));
        product.setCategory2(getStringValue(str, 2));
        product.setCategory3(getStringValue(str, 3));
        product.setBrand(getStringValue(str, 4));
        product.setModel(getStringValue(str, 5));
        product.setPrice(StringUtil.cleanAndReplace(getStringValue(str, 6),"."));
        product.setCity(getStringValue(str, 7));
        product.setCompetitor(getStringValue(str, 8));
        product.setTime(getStringValue(str, 9));
        product.setDate(getStringValue(str, 10));
        product.setCompetitorPrice(StringUtil.cleanAndReplace(getStringValue(str, 11),"."));
        product.setCompetitorOldPrice(StringUtil.cleanAndReplace(getStringValue(str, 12),"."));
        product.setCompetitorActionPrice(StringUtil.cleanAndReplace(getStringValue(str, 13),"."));
        product.setComment(getStringValue(str, 14));
        product.setCompetitorModel(getStringValue(str, 15));
        product.setYearCompetitor(getStringValue(str, 16));
        product.setAnalogue(getStringValue(str, 17));
        product.setAddressOfTheCompetitor(getStringValue(str, 18));
        product.setStatus(getStringValue(str, 19));
        product.setPromo(getStringValue(str, 20));
        product.setCompetitorUrl(getStringValue(str, 21));
        product.setClientUrl(getStringValue(str, 22));
        product.setWebCacheUrl(getStringValue(str, 23));
        return product;
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }
    @Override
    public String saveAll(String filePath, File transferTo, HttpServletResponse response, String... additionalParams) throws IOException {
        return null;
    }

    @Override
    public String deleteAll() {
        return null;
    }
}
