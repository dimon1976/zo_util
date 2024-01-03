package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.impl.av.AvHandbookService;
import by.demon.zoom.service.impl.av.AvReportService;
import by.demon.zoom.service.impl.MegatopService;
import by.demon.zoom.service.impl.av.AvTaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/clients")
public class ClientsController {


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
        model.addAttribute("date", LocalDate.now());
        model.addAttribute("lenta", new Lenta());
        return "/clients/lenta";
    }

    @GetMapping("/megatop")
    public String megatop(Model model) {
//        model.addAttribute("fileForm", new FileForm());
        model.addAttribute("latestLabels", megatopService.getLatestLabels());
        model.addAttribute("generatedLabel", megatopService.generateLabel());
        return "/clients/megatop";
    }

    @GetMapping("/simple")
    public String simple() {
        return "/clients/simple";
    }

    @GetMapping("/av")
    public String av(Model model) {
        model.addAttribute("reports", avReportService.getLatestReport());
        model.addAttribute("tasks", avTaskService.getLatestTask());
        model.addAttribute("retailNetworkCode", avHandbookService.getRetailNetworkCode());
        return "/clients/av";
    }
}
