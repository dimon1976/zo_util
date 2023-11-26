package by.demon.zoom.controller;

import by.demon.zoom.domain.Edadeal;
import by.demon.zoom.domain.FileForm;
import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.impl.AvService;
import by.demon.zoom.service.impl.MegatopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientsController {


    private final MegatopService megatopService;
    private final AvService avService;


    public ClientsController(MegatopService megatopService, AvService avService) {
        this.megatopService = megatopService;
        this.avService = avService;
    }

    @GetMapping("/lenta")
    public String lenta(Model model) {
        model.addAttribute("lenta", new Lenta());
        model.addAttribute("edadeal", new Edadeal());
        return "/clients/lenta";
    }

    @GetMapping("/megatop")
    public String megatop(Model model) {
        model.addAttribute("fileForm", new FileForm());
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
        model.addAttribute("tasks", avService.getLatestTasks());
        return "/clients/av";
    }
}
