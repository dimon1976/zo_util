package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.ReportSummaryRepository;
import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import by.demon.zoom.domain.imp.av.ReportSummary;
import by.demon.zoom.service.FileProcessingService;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportSummaryService implements FileProcessingService<ReportSummary> {
    private final ReportSummaryRepository reportSummaryRepository;
    private final static Logger log = LoggerFactory.getLogger(ReportSummaryService.class);

    public ReportSummaryService(ReportSummaryRepository reportSummaryRepository) {
        this.reportSummaryRepository = reportSummaryRepository;
    }

    @Override
    public ArrayList<ReportSummary> readFiles(List<File> files, String... additionalParams) throws IOException {
        return null;
    }

    @Override
    public String save(ArrayList<ReportSummary> reportSummaryArrayList) {
        try {
            reportSummaryRepository.saveAll(reportSummaryArrayList);
            log.info("Saved {} reportSummary entities", reportSummaryArrayList.size());
            return "The job file has been successfully saved";
        } catch (Exception e) {
            log.error("Error saving reports", e);
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                String sql = ((ConstraintViolationException) cause).getSQL();
                int index = sql.indexOf("value too long for column");
                log.error("Error saving reportSummary at line: {}", index);
            }
            throw new RuntimeException("Failed to save reportSummary", e);
        }
    }

    public List<ReportSummary> findAllByCityAndTypeReport(String city, String typeReport) {
        List<ReportSummary> byCityAndTypeReport = reportSummaryRepository.findByCityAndTypeReport(city, typeReport);
        return byCityAndTypeReport;
    }

    public List<String> findDistinctRetailChains() {
        return reportSummaryRepository.findDistinctRetailChains();
    }

    public ArrayList<ReportSummary> getReportSummary(ArrayList<CsvAvReportEntity> reportEntities) {
        Map<String, ReportSummary> summaryMap = new HashMap<>();
        for (CsvAvReportEntity entity : reportEntities) {
            String retailChain = entity.getRetailChain();
            ReportSummary summary = summaryMap.getOrDefault(retailChain, new ReportSummary());
            summary.setRetailChain(retailChain);

            // Увеличиваем счетчики в зависимости от условий
            summary.setCountRows(summary.getCountRows() + 1);
            if (entity.getCompetitorsPrice() != null && !entity.getCompetitorsPrice().isEmpty()) {
                summary.setCountCompetitorsPrice(summary.getCountCompetitorsPrice() + 1);
            }
            if (entity.getPromotionalPrice() != null && !entity.getPromotionalPrice().isEmpty()) {
                summary.setCountPromotionalPrice(summary.getCountPromotionalPrice() + 1);
            }
            summary.setTypeReport(entity.getTypeReport());
            summary.setCity(entity.getCity());
            summary.setTask_no(entity.getJobNumber());
            summary.setUploadTime(entity.getUploadTime());
            summaryMap.put(retailChain, summary);
        }

        return new ArrayList<>(summaryMap.values());
    }
}