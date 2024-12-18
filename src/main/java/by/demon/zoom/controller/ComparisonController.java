package by.demon.zoom.controller;

import by.demon.zoom.domain.imp.av.ReportSummary;
import by.demon.zoom.service.impl.av.ReportSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ComparisonController {
    private final ReportSummaryService reportSummaryService;
    private static final Logger logger = LoggerFactory.getLogger(ComparisonController.class);

    public ComparisonController(ReportSummaryService reportSummaryService) {
        this.reportSummaryService = reportSummaryService;
    }

    @PostMapping("/excel/av/comparison")
    public String showComparisonPage(Model model, HttpServletRequest request) {
        String cityId = request.getParameter("city");
        String typeReport = request.getParameter("typeReport");
        Double threshold = Double.parseDouble(request.getParameter("threshold"));

        // Получение данных из сервиса, упорядоченных по времени загрузки
        List<ReportSummary> reports = reportSummaryService.findAllByCityAndTypeReport(cityId, typeReport);
        logger.info("Reports: {}", reports);

        // Получение уникальных retailChain
        List<String> retailChains = reports.stream()
                .map(ReportSummary::getRetailChain)
                .distinct()
                .collect(Collectors.toList());
        logger.info("Retail Chains: {}", retailChains);

        // Группировка отчетов по task_no и сортировка внутри каждой группы по uploadTime
        Map<String, List<ReportSummary>> taskGroups = reports.stream()
                .collect(Collectors.groupingBy(ReportSummary::getTask_no,
                        LinkedHashMap::new, // Сохраняет порядок вставки групп
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            list.sort(Comparator.comparing(ReportSummary::getUploadTime)); // Сортировка по возрастанию
                            return list;
                        })));
        logger.info("Task Groups: {}", taskGroups);

        model.addAttribute("retailChains", retailChains);
        model.addAttribute("taskGroups", taskGroups);
        model.addAttribute("threshold", threshold);

        return "/clients/av/comparisonTable";
    }

}
