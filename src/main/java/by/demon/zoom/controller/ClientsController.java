package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.impl.MegatopService;
import by.demon.zoom.service.impl.av.AvHandbookService;
import by.demon.zoom.service.impl.av.AvReportService;
import by.demon.zoom.service.impl.av.AvTaskService;
import by.demon.zoom.util.MethodPerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;

@Controller
@RequestMapping("/clients")
public class ClientsController {
    private static final Logger logger = LoggerFactory.getLogger(ClientsController.class);
    private final MegatopService megatopService;
    private final AvReportService avReportService;
    private final AvTaskService avTaskService;
    private final AvHandbookService avHandbookService;

    public ClientsController(MegatopService megatopService, AvReportService avReportService, AvTaskService avTaskService, AvHandbookService avHandbookService) {
        this.megatopService = megatopService;
        this.avReportService = avReportService;
        this.avTaskService = avTaskService;
        this.avHandbookService = avHandbookService;
    }


    @GetMapping("/lenta")
    public String lenta(Model model) {
        try {
            model.addAttribute("date", LocalDate.now());
            model.addAttribute("lenta", new Lenta());
        } catch (Exception e) {
            logger.error("Error executing lenta method", e);
            model.addAttribute("error", "Failed to load lenta data");
        }
        return "/clients/lenta";
    }

    @GetMapping("/megatop")
    public String megatop(Model model) {
        logger.info("Executing megatop method");
        try {
            model.addAttribute("latestLabels", megatopService.getLatestLabels());
            model.addAttribute("generatedLabel", megatopService.generateLabel());
        } catch (Exception e) {
            logger.error("Error executing megatop method", e);
            model.addAttribute("error", "Failed to load megatop data");
        }
        return "/clients/megatop";
    }

    @GetMapping("/simple")
    public String simple() {
        logger.info("Executing simple method");
        return "/clients/simple";
    }

    @GetMapping("/av")
    public String av(Model model) {
        logger.info("Executing av method");
        try {
            model.addAttribute("reports", avReportService.getLatestReport());
            Instant start = MethodPerformance.start();
            model.addAttribute("tasks", avTaskService.getLatestTask());
            MethodPerformance.finish(start, "task");
            model.addAttribute("retailNetworkCode", avHandbookService.getRetailNetworkCode());
        } catch (Exception e) {
            logger.error("Error executing av method", e);
            model.addAttribute("error", "Failed to load av data");
        }
        return "/clients/av";
    }
}
