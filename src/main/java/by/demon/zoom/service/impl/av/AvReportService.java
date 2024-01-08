package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvReportRepository;
import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class AvReportService implements FileProcessingService<CsvAvReportEntity> {


    private final static Logger log = LoggerFactory.getLogger(AvReportService.class);
    private final List<String> header = Arrays.asList("Номер задания","Старт задания","Окончание задания","Товар НО","Категория","Код товарной категории","Описание товара","Комментарий по товару","Бренд","Код ценовой зоны","Код розничной сети","Розничная сеть","Регион","Физический адрес","Штрихкод",
            "Количество штук","Цена конкурента","Цена акционная/по карте","Аналог","Нет товара","Дата мониторинга","Фото","Примечание","Ссылка на страницу товара");
    private final AvReportRepository avReportRepository;
    private final DataDownload dataDownload;
    private final DataToExcel<CsvAvReportEntity> dataToExcel;
    private final AvTaskRepository avTaskRepository;

    public AvReportService(AvReportRepository avReportRepository, DataDownload dataDownload, DataToExcel<CsvAvReportEntity> dataToExcel, AvTaskRepository avTaskRepository) {
        this.avReportRepository = avReportRepository;
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
        this.avTaskRepository = avTaskRepository;
    }

    public void download(ArrayList<CsvAvReportEntity> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = DataDownload.getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
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

    @Override
    public ArrayList<CsvAvReportEntity> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<CsvAvReportEntity> allReports = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<CsvAvReportEntity>>> futures = files.stream()
                .map(file -> executorService.submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        List<List<Object>> lists = readDataFromFile(file);
                        Files.delete(file.toPath());
                        return getReportList(lists);
                    } catch (IOException e) {
                        log.error("Error reading data from file: {}", file.getAbsolutePath(), e);
                    } catch (Exception e) {
                        log.error("Error processing file: {}", file.getAbsolutePath(), e);
                    }
                    return null;
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<CsvAvReportEntity>> future : futures) {
            try {
                ArrayList<CsvAvReportEntity> reportArrayList = future.get();
                allReports.addAll(reportArrayList);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
            }
        }
        executorService.shutdown();

        try {
            save(allReports);
            return allReports;
        } catch (Exception e) {
            log.error("Error saving tasks", e);
            throw new RuntimeException("Failed to save tasks", e);
        }
    }

    public ArrayList<CsvAvReportEntity> getDto(String... additionalParameters) {
        List<CsvAvReportEntity> reportByLabelAvDataEntity = getAllByJobNumber(additionalParameters[0]);
        return getAvDataEntityDTOList(reportByLabelAvDataEntity, additionalParameters[0]);
    }

    private ArrayList<CsvAvReportEntity> getAvDataEntityDTOList(List<CsvAvReportEntity> avDataEntityList, String jobNumber) {
        ArrayList<CsvAvReportEntity> result = new ArrayList<>();
        List<String> retailerCodeFromTask = getRetailerCodeFromTask(jobNumber);
        for (CsvAvReportEntity entity : avDataEntityList) {
            if (ifExistCompetitor(entity.getRetailerCode(), retailerCodeFromTask)) {
                result.add(entity);
            }
        }
        return result;
    }

    public static Boolean ifExistCompetitor(String str, List<String> list) {
        return list.stream()
                .anyMatch(i -> i.equals(str));
    }

    @Transactional
    public void deleteReport(String reportNum){
        avReportRepository.deleteAllByField(reportNum);
    }


    @Override
    public String save(ArrayList<CsvAvReportEntity> reportArrayList) {
        try {
            avReportRepository.saveAll(reportArrayList);
            log.info("Job file has been successfully saved");
            return "The job file has been successfully saved";
        } catch (Exception e) {
            log.error("Error saving tasks", e);

            // Получите первопричину исключения
            Exception cause = (Exception) e.getCause();

            // Если первопричина - это ConstraintViolationException
            if (cause instanceof ConstraintViolationException) {

                // Получите SQL-запрос, который был выполнен при попытке сохранить объект
                String sql = ((ConstraintViolationException) cause).getSQL();

                // Найдите индекс первого символа ошибки в строке
                int index = sql.indexOf("value too long for column");

                // Логируйте индекс строки с ошибкой
                log.error("Error saving task at line: {}", index);

                throw new RuntimeException("Failed to save tasks", e);
            }

            throw new RuntimeException("Failed to save tasks", e);
        }
    }


    private ArrayList<CsvAvReportEntity> getReportList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createReportFromList)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //

    private List<String> convert(List<CsvAvReportEntity> objectList) {
        return objectList.stream()
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }


    private CsvAvReportEntity createReportFromList(List<Object> str) {
        CsvAvReportEntity csvReportEntity = new CsvAvReportEntity();
        csvReportEntity.setJobNumber(getStringValue(str, 0));
        csvReportEntity.setJobStart(getStringValue(str, 1));
        csvReportEntity.setJobEnd(getStringValue(str, 2));
        csvReportEntity.setItemNumber(getStringValue(str, 3));
        csvReportEntity.setCategory(getStringValue(str, 4));
        csvReportEntity.setProductCategoryCode(getStringValue(str, 5));
        csvReportEntity.setProductDescription(getStringValue(str, 6));
        csvReportEntity.setProductComment(getStringValue(str, 7));
        csvReportEntity.setBrand(getStringValue(str, 8));
        csvReportEntity.setPriceZoneCode(getStringValue(str, 9));
        csvReportEntity.setRetailerCode(getStringValue(str, 10));
        csvReportEntity.setRetailChain(getStringValue(str, 11));
        csvReportEntity.setRegion(getStringValue(str, 12));
        csvReportEntity.setPhysicalAddress(getStringValue(str, 13));
        csvReportEntity.setBarcode(getStringValue(str, 14));
        csvReportEntity.setQuantityOfPieces(getStringValue(str, 15));
        csvReportEntity.setCompetitorsPrice(getStringValue(str, 16));
        csvReportEntity.setPromotionalPrice(getStringValue(str, 17));
        csvReportEntity.setAnalogue(getStringValue(str, 18));
        csvReportEntity.setNoProduct(getStringValue(str, 19));
        csvReportEntity.setMonitoringDate(getStringValue(str, 20));
        csvReportEntity.setPhoto(getStringValue(str, 21));
        csvReportEntity.setNote(getStringValue(str, 22));
        csvReportEntity.setLinkToProductPage(getStringValue(str, 23));
        return csvReportEntity;
    }


    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }

    public List<String> getLatestReport() {
        // Получаем последние 10 сохраненных заданий из базы данных
        Pageable pageable = PageRequest.of(0, 10);
        return avReportRepository.findDistinctTopByJobNumber(pageable);
    }

    public List<String> getRetailerCodeFromTask(String task) {
        return avTaskRepository.findDistinctByJobNumber(task);
    }

    public List<CsvAvReportEntity> getAllByJobNumber(String jobNumber) {
        return avReportRepository.findAllByJobNumber(jobNumber);
    }

}
