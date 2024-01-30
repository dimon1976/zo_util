package by.demon.zoom.service.impl;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.dto.imp.SimpleDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.Globals.TEMP_PATH;

@Service
public class SimpleService implements FileProcessingService<SimpleDTO> {

    private static final Logger log = LoggerFactory.getLogger(SimpleService.class);
    private final DataDownload dataDownload;
    private final DataToExcel<SimpleDTO> dataToExcel;

    private final List<String> header = Arrays.asList("ID", "Категория 1", "Категория 2", "Категория 3", "Бренд", "Модель", "Цена Simplewine на дату парсинга", "Город", "Конкурент", "Время выкачки"
            , "Дата", "Цена конкурента регуляр (по карте)", "Цена конкурента без карты", "Цена конкурента промо/акция"
            , "Комментарий", "Наименование товара конкурента", "Год конкурента", "Аналог", "Адрес конкурента"
            , "Статус товара (В наличии/Под заказ/Нет в наличии)", "Промо (да/нет)", "Ссылка конкурент", "Ссылка Симпл", "Скриншот");

    public SimpleService(DataDownload dataDownload, DataToExcel<SimpleDTO> dataToExcel) {
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<SimpleDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<SimpleDTO> allUrlDTOs = new ArrayList<>(); // Создаем переменную для сохранения всех DTO
        for (File file : files) {
            try {
                List<List<Object>> lists = readDataFromFile(file);
                Collection<Product> productList = getProductList(lists);
                Collection<SimpleDTO> collect = getSimpleDTOList(productList);
                allUrlDTOs.addAll(collect);
                log.info("File {} successfully read", file.getName());

            } catch (IOException e) {
                log.error("Error reading data from file: {}", file.getAbsolutePath(), e);

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

    public void download(ArrayList<SimpleDTO> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        String orgName = additionalParameters[0];
        String s = orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(0, orgName.lastIndexOf("."));
        Path path = Path.of(TEMP_PATH, s + (format.equals("excel") ? ".xlsx" : ".csv"));
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportToExcel(header, list, out, 0);
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

    private static List<String> convert(List<SimpleDTO> objectList) {
        return objectList.stream()
                .filter(Objects::nonNull)
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }

    @Override
    public String save(ArrayList<SimpleDTO> collection) {
        return null;
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
        product.setPrice(StringUtil.cleanAndReplace(getStringValue(str, 6), "."));
        product.setCity(getStringValue(str, 7));
        product.setCompetitor(getStringValue(str, 8));
        product.setTime(getStringValue(str, 9));
        product.setDate(getStringValue(str, 10));
        product.setCompetitorPrice(StringUtil.cleanAndReplace(getStringValue(str, 11), "."));
        product.setCompetitorOldPrice(StringUtil.cleanAndReplace(getStringValue(str, 12), "."));
        product.setCompetitorActionPrice(StringUtil.cleanAndReplace(getStringValue(str, 13), "."));
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
}
