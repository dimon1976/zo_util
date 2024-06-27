package by.demon.zoom.domain.av;

import by.demon.zoom.dao.CsvAvReportRepository;
import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import by.demon.zoom.dto.CsvAvReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final CsvAvReportRepository csvAvReportRepository;

    @Autowired
    public ReportService(CsvAvReportRepository csvAvReportRepository) {
        this.csvAvReportRepository = csvAvReportRepository;
    }

    public List<CsvAvReportDTO> getCsvAvReport() {
        List<CsvAvReportEntity> entities = csvAvReportRepository.findAll();
        Map<String, CsvAvReportDTO> reportMap = new HashMap<>();

        for (CsvAvReportEntity entity : entities) {
            String key = entity.getJobNumber() + "-" + entity.getJobEnd();
            CsvAvReportDTO dto = reportMap.getOrDefault(key, new CsvAvReportDTO(entity.getJobNumber(), entity.getJobEnd()));
            try {
                if (entity.getCompetitorsPrice() != null && !entity.getCompetitorsPrice().isEmpty()) {
                    double price = Double.parseDouble(entity.getCompetitorsPrice());
                    dto.addCompetitorPrice(entity.getRetailChain(), price);
                    reportMap.put(key, dto);
                } else {
                    // Log the error and continue
                    System.err.println("Empty or null price for entity: " + entity);
                }
            } catch (NumberFormatException e) {
                // Log the error and continue
                System.err.println("Failed to parse price: " + entity.getCompetitorsPrice());
            }
        }

        return new ArrayList<>(reportMap.values());
    }
}
