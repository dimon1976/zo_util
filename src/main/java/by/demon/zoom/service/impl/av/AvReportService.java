package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvReportRepository;
import by.demon.zoom.domain.CsvRow;
import by.demon.zoom.domain.av.CsvReportEntity;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class AvReportService implements FileProcessingService<CsvReportEntity> {


    private final static Logger log = LoggerFactory.getLogger(AvReportService.class);

    private final AvReportRepository avReportRepository;
    private final DataDownload dataDownload;

    public AvReportService(AvReportRepository avReportRepository, DataDownload dataDownload) {
        this.avReportRepository = avReportRepository;
        this.dataDownload = dataDownload;
    }

    public void download(HttpServletResponse response, Path path, String format, String... additionalParams) throws IOException {
        List<CsvReportEntity> allByJobNumber = avReportRepository.findAllByJobNumber(additionalParams[1]);
        List<String> strings = convert(allByJobNumber);
//        List<CsvDataEntity> dataEntities = avTaskRepository.findByJobNumberAndRetailerCode("TASK-00003539", "ЯНДЕКС_М_ОНЛ");

//        dataDownload.download(strings, path.toFile(), response, "");
    }

    @Override
    public ArrayList<CsvReportEntity> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<CsvReportEntity> allReports = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<CsvReportEntity>>> futures = files.stream()
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

        for (Future<ArrayList<CsvReportEntity>> future : futures) {
            try {
                ArrayList<CsvReportEntity> reportArrayList = future.get();
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

    @Override
    public String save(ArrayList<CsvReportEntity> reportArrayList) {
        try {
            avReportRepository.saveAll(reportArrayList);
            log.info("Job file has been successfully saved");
            return "The job file has been successfully saved";
        } catch (Exception e) {
            log.error("Error saving tasks", e);
            throw new RuntimeException("Failed to save tasks", e);
        }
    }


    private ArrayList<CsvReportEntity> getReportList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createReportFromList)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //

    public static List<String> convert(List<CsvReportEntity> objectList) {
        return objectList.stream()
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }


    private CsvReportEntity createReportFromList(List<Object> str) {
        CsvReportEntity csvReportEntity = new CsvReportEntity();
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


}
