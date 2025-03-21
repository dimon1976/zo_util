package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvReportRepository;
import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import by.demon.zoom.domain.imp.av.ReportSummary;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
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
import static by.demon.zoom.util.FileUtil.downloadFile;
import static by.demon.zoom.util.FileUtil.getPath;

@Service
public class AvReportService implements FileProcessingService<CsvAvReportEntity> {


    private final static Logger log = LoggerFactory.getLogger(AvReportService.class);
    private final List<String> header = Arrays.asList("Номер задания", "Старт задания", "Окончание задания", "Товар НО", "Категория", "Код товарной категории", "Описание товара", "Комментарий по товару", "Бренд", "Код ценовой зоны", "Код розничной сети", "Розничная сеть", "Регион", "Физический адрес", "Штрихкод",
            "Количество штук", "Цена конкурента", "Цена акционная/по карте", "Аналог", "Нет товара", "Дата мониторинга", "Фото", "Примечание", "Ссылка на страницу товара");
    private final AvReportRepository avReportRepository;
    private String[] params;
    private final DataToExcel<CsvAvReportEntity> dataToExcel;
    private final AvTaskRepository avTaskRepository;
    private final ReportSummaryService reportSummary;

    public AvReportService(AvReportRepository avReportRepository, DataToExcel<CsvAvReportEntity> dataToExcel, AvTaskRepository avTaskRepository, ReportSummaryService reportSummary) {
        this.avReportRepository = avReportRepository;
        this.dataToExcel = dataToExcel;
        this.avTaskRepository = avTaskRepository;
        this.reportSummary = reportSummary;
    }

    public void download(ArrayList<CsvAvReportEntity> list, HttpServletResponse response, String format) throws IOException {
        String fileName = "av_report_data";
        Path path = getPath(fileName, format.equals("excel") ? ".xlsx" : ".csv");
        downloadFile(header, list, response, format, path, dataToExcel);
    }


    @Override
    public ArrayList<CsvAvReportEntity> readFiles(List<File> files, String... additionalParams) throws IOException {
        params = additionalParams;
        ArrayList<CsvAvReportEntity> allReports = new ArrayList<>();
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<ArrayList<CsvAvReportEntity>>> futures = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (File file : files) {
            futures.add(executorService.submit(() -> {
                try {
                    List<List<Object>> lists = readDataFromFile(file);
                    Files.delete(file.toPath());
                    log.info("File {} removed successfully.", file.getAbsolutePath());
                    ArrayList<CsvAvReportEntity> reportList = getReportList(lists);
                    save(reportList);
                    log.info("File {} {} processed and saved successfully {} lines.", file.getName(), reportList.get(1).getJobNumber(), reportList.size());
                    return reportList;
                } catch (Exception e) {
                    log.error("Failed to process file: {}", file.getAbsolutePath(), e);
                    errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                    return new ArrayList<>();
                }
            }));
        }

        for (Future<ArrayList<CsvAvReportEntity>> future : futures) {
            try {
                allReports.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }
        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }
        return allReports;
    }

    public ArrayList<CsvAvReportEntity> getDto(String... additionalParameters) {
        try {
            String jobNumber = additionalParameters[0];
            log.info("Fetching reports for job number: {}", jobNumber);
            List<CsvAvReportEntity> reportByLabelAvDataEntity = getAllByJobNumber(jobNumber);
            return getAvDataEntityDTOList(reportByLabelAvDataEntity, jobNumber);
        } catch (Exception e) {
            log.error("Error fetching reports", e);
            throw new RuntimeException("Error fetching reports", e);
        }
    }

    private ArrayList<CsvAvReportEntity> getAvDataEntityDTOList(List<CsvAvReportEntity> avDataEntityList, String jobNumber) {
        reportSummary.deleteReportSummary(jobNumber);
        ArrayList<CsvAvReportEntity> result = new ArrayList<>();
        List<String> retailerCodeFromTask = getRetailerCodeFromTask(jobNumber);
        for (CsvAvReportEntity entity : avDataEntityList) {
            if (ifExistCompetitor(entity.getRetailerCode(), retailerCodeFromTask)) {
                result.add(entity);
            }
        }
        ArrayList<ReportSummary> summary = reportSummary.getReportSummary(result);
        reportSummary.save(summary);
        return result;
    }

    public static Boolean ifExistCompetitor(String str, List<String> list) {
        return list.stream()
                .anyMatch(i -> i.equals(str));
    }

    @Transactional
    public int deleteReport(String reportNum) {
        try {
            log.info("Deleting report with number: {}", reportNum);
            int deletedCount = avReportRepository.deleteAllByField(reportNum);
            log.info("Deleted {} report(s) with number: {}", deletedCount, reportNum);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error deleting report with number: {}", reportNum, e);
            throw new RuntimeException("Error deleting report with number: " + reportNum, e);
        }
    }


    @Override
    public String save(ArrayList<CsvAvReportEntity> reportArrayList) {
        try {
            avReportRepository.saveAll(reportArrayList);
            log.info("Saved {} report entities", reportArrayList.size());
            return "The job file has been successfully saved";
        } catch (Exception e) {
            log.error("Error saving reports", e);
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                String sql = ((ConstraintViolationException) cause).getSQL();
                int index = sql.indexOf("value too long for column");
                log.error("Error saving task at line: {}", index);
            }
            throw new RuntimeException("Failed to save reports", e);
        }
    }


    private ArrayList<CsvAvReportEntity> getReportList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createReportFromList)
                .collect(Collectors.toCollection(ArrayList::new));
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
        if (params != null) {
            csvReportEntity.setCity(params[0]);
            csvReportEntity.setTypeReport(params[1]);
        }
        return csvReportEntity;
    }


    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }

    public List<String> getLatestReport() {
        // Получаем последние 10 сохраненных заданий из базы данных
//        Pageable pageable = PageRequest.of(0, 20);
        return avReportRepository.findDistinctTopByJobNumber();
    }

    public List<String> getRetailerCodeFromTask(String task) {
        return avTaskRepository.findDistinctByJobNumber(task);
    }

    public List<CsvAvReportEntity> getAllByJobNumber(String jobNumber) {
        return avReportRepository.findAllByJobNumber(jobNumber);
    }
}
