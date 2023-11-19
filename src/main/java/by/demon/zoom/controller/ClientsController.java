package by.demon.zoom.controller;

import by.demon.zoom.domain.Edadeal;
import by.demon.zoom.domain.FileForm;
import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.impl.MegatopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientsController {

    @Autowired
    private MegatopService megatopService;


//    public ClientsController(MegatopService megatopService) {
//        this.megatopService = megatopService;
//    }

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
}
