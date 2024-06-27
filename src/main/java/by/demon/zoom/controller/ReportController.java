package by.demon.zoom.controller;

import by.demon.zoom.domain.av.ReportService;
import by.demon.zoom.dto.CsvAvReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/csv-av-report")
    public List<CsvAvReportDTO> getCsvAvReport() {
        return reportService.getCsvAvReport();
    }
}
