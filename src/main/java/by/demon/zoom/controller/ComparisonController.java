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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ComparisonController {
    private final ReportSummaryService reportSummaryService;
    private static final Logger logger = LoggerFactory.getLogger(ComparisonController.class);
    private static final double THRESHOLD_PERCENT = 10.0; // Пороговое значение в процентах

    public ComparisonController(ReportSummaryService reportSummaryService) {
        this.reportSummaryService = reportSummaryService;
    }

    @PostMapping("/excel/av/comparison")
    public String showComparisonPage(Model model, HttpServletRequest request) {
        // Получение данных из сервиса, упорядоченных по времени загрузки
        String cityId = request.getParameter("city");
        String typeReport = request.getParameter("typeReport");

        List<ReportSummary> reportSummaries = reportSummaryService.findAllByCityAndTypeReport(cityId, typeReport);
        reportSummaries.sort(Comparator.comparing(ReportSummary::getUploadTime).reversed());

        Map<String, List<ReportSummary>> reportSummaryMap = reportSummaries.stream()
                .collect(Collectors.groupingBy(ReportSummary::getRetailChain));

        List<String> taskNos = reportSummaries.stream()
                .map(ReportSummary::getTask_no)
                .distinct()
                .collect(Collectors.toList());


        for (String retailChain : reportSummaryMap.keySet()) {
            List<ReportSummary> summaries = reportSummaryMap.get(retailChain);
            for (int i = 0; i < summaries.size(); i++) {
                if (i > 0) {
                    ReportSummary current = summaries.get(i);
                    ReportSummary previous = summaries.get(i - 1);

                    current.setHighlightCountRows(isSignificantDecrease(previous.getCountRows(), current.getCountRows()));
                    current.setHighlightCountCompetitorsPrice(isSignificantDecrease(previous.getCountCompetitorsPrice(), current.getCountCompetitorsPrice()));
                    current.setHighlightCountPromotionalPrice(isSignificantDecrease(previous.getCountPromotionalPrice(), current.getCountPromotionalPrice()));
                }
            }
        }

        model.addAttribute("reportSummaries", reportSummaries);
        model.addAttribute("taskNos", taskNos);
        model.addAttribute("reportSummaryMap", reportSummaryMap);

        return "/clients/av"; // Название HTML-шаблона Thymeleaf
    }
    private boolean isSignificantDecrease(long previousValue, long currentValue) {
        if (previousValue == 0) {
            return false;
        }
        double decreasePercent = ((double) (previousValue - currentValue) / previousValue) * 100;
        return decreasePercent > THRESHOLD_PERCENT;
    }

}
