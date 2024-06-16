package by.demon.zoom.service.impl;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.dto.imp.SimpleDTO;
import by.demon.zoom.mapper.MappingUtils;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import by.demon.zoom.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileDownloadUtil.downloadFile;
import static by.demon.zoom.util.Globals.TEMP_PATH;

@Service
public class SimpleService implements FileProcessingService<SimpleDTO> {

    private static final Logger log = LoggerFactory.getLogger(SimpleService.class);
    private final DataToExcel<SimpleDTO> dataToExcel;

    private final List<String> header = Arrays.asList("ID", "Категория 1", "Категория 2", "Категория 3", "Бренд", "Модель", "Цена Simplewine на дату парсинга", "Город", "Конкурент", "Время выкачки"
            , "Дата", "Цена конкурента регуляр (по карте)", "Цена конкурента без карты", "Цена конкурента промо/акция"
            , "Комментарий", "Наименование товара конкурента", "Год конкурента", "Аналог", "Адрес конкурента"
            , "Статус товара (В наличии/Под заказ/Нет в наличии)", "Промо (да/нет)", "Ссылка конкурент", "Ссылка Симпл", "Скриншот");

    public SimpleService(DataToExcel<SimpleDTO> dataToExcel) {
        this.dataToExcel = dataToExcel;
    }

    @Override
    public ArrayList<SimpleDTO> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<SimpleDTO> allUrlDTOs = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<ArrayList<SimpleDTO>>> futures = files.stream()
                .map(file -> executorService.submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        List<List<Object>> lists = readDataFromFile(file);
                        Files.delete(file.toPath());
                        Collection<Product> productList = getProductList(lists);
                        Collection<SimpleDTO> collect = getSimpleDTOList(productList);
                        log.info("File {} successfully read", file.getName());
                        return new ArrayList<>(collect);
                    } catch (Exception e) {
                        log.error("Error processing file: {}", file.getAbsolutePath(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                        return new ArrayList<SimpleDTO>();
                    }
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<SimpleDTO>> future : futures) {
            try {
                allUrlDTOs.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }
        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }

        return allUrlDTOs;
    }

    public void download(ArrayList<SimpleDTO> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        String orgName = additionalParameters[0];
        String s = orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(0, orgName.lastIndexOf("."));
        Path path = Path.of(TEMP_PATH, s + (format.equals("excel") ? ".xlsx" : ".csv"));
        downloadFile(header, list, response, format, path, dataToExcel);
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
