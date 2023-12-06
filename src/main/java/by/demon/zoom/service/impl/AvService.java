package by.demon.zoom.service.impl;

import by.demon.zoom.dao.AvReportRepository;
import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.dao.HandbookRepository;
import by.demon.zoom.domain.CsvRow;
import by.demon.zoom.domain.av.CsvDataEntity;
import by.demon.zoom.domain.av.CsvReportEntity;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.MethodPerformance;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class AvService implements FileProcessingService {


    private final static Logger LOG = LoggerFactory.getLogger(AvService.class);
    private final AvTaskRepository avTaskRepository;
    private final AvReportRepository avReportRepository;
    private final HandbookRepository handbookRepository;
    private final DataDownload dataDownload;

    public AvService(AvTaskRepository avTaskRepository, AvReportRepository avReportRepository, HandbookRepository handbookRepository, DataDownload dataDownload) {
        this.avTaskRepository = avTaskRepository;
        this.avReportRepository = avReportRepository;
        this.handbookRepository = handbookRepository;
        this.dataDownload = dataDownload;
    }

    @Override
    public String readFile(Path path, HttpServletResponse response, String... additionalParams) throws IOException {
        List<List<Object>> lists = readDataFromFile(path.toFile());
        switch (additionalParams[0]) {
            case "task":
                Collection<CsvDataEntity> taskArrayList = getTaskList(lists);
                Long start = MethodPerformance.start();
                avTaskRepository.saveAll(taskArrayList);
                MethodPerformance.finish(start, "Сохранение в БД задачи");
                break;
            case "report":
                Collection<CsvReportEntity> reportArrayList = getReportList(lists);
                avReportRepository.saveAll(reportArrayList);
                break;
            default:
                return "Invalid additional parameter.";
        }
        LOG.info("File {} processed and saved successfully.", path.getFileName());
        return "File processed and saved successfully.";
    }

    @Override
    public void download(HttpServletResponse response, Path path, String format, String... additionalParams) throws IOException {
        List<CsvReportEntity> allByJobNumber = avReportRepository.findAllByJobNumber(additionalParams[1]);
        List<String> strings = convert(allByJobNumber);
//        List<CsvDataEntity> dataEntities = avTaskRepository.findByJobNumberAndRetailerCode("TASK-00003539", "ЯНДЕКС_М_ОНЛ");

//        dataDownload.download(strings, path.toFile(), response, "");
    }

    @Override
    public void save(Collection<T> collection) {

    }

    @Override
    public Collection<T> getData() {
        return null;
    }

    private Collection<CsvDataEntity> getTaskList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createTaskFromList)
                .collect(Collectors.toList());
    }

    private Collection<CsvReportEntity> getReportList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createReportFromList)
                .collect(Collectors.toList());
    }

    //

    public static List<String> convert(List<CsvReportEntity> objectList) {
        return objectList.stream()
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }

    private CsvDataEntity createTaskFromList(List<Object> str) {
        CsvDataEntity task = new CsvDataEntity();
        task.setJobNumber(getStringValue(str, 0));
        task.setJobStart(getStringValue(str, 1));
        task.setJobEnd(getStringValue(str, 2));
        task.setItemNumber(getStringValue(str, 3));
        task.setCategory(getStringValue(str, 4));
        task.setProductCategoryCode(getStringValue(str, 5));
        task.setProductDescription(getStringValue(str, 6));
        task.setProductComment(getStringValue(str, 7));
        task.setBrand(getStringValue(str, 8));
        task.setPriceZoneCode(getStringValue(str, 9));
        task.setRetailerCode(getStringValue(str, 10));
        task.setRetailChain(getStringValue(str, 11));
        task.setRegion(getStringValue(str, 12));
        task.setPhysicalAddress(getStringValue(str, 13));
        task.setBarcode(getStringValue(str, 14));
        return task;
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

    public List<String> getLatestTask() {
        // Получаем последние 10 сохраненных заданий из базы данных
        Pageable pageable = PageRequest.of(0, 10);
        return avTaskRepository.findDistinctTopByJobNumber(pageable);
    }

    public List<String> getRetailNetwork() {
        return handbookRepository.findDistinctByRetailNetwork();
    }
}
