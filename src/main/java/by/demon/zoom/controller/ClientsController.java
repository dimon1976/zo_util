package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientsController {



    @GetMapping("/lenta")
    public String lenta(Model model) {
//        model.addAttribute("date", new Date());
//        model.addAttribute("localDateTime", LocalDateTime.now());
//        model.addAttribute("localDate", LocalDate.now());
//        model.addAttribute("timestamp", Instant.now());
        model.addAttribute("lenta", new Lenta());
        return "/clients/lenta";
    }
    @GetMapping("/megatop")
    public String megatop() {
        return "/clients/megatop";
    }
    @GetMapping("/detmir")
    public String detmir() {
        return "/clients/detmir";
    }
    @GetMapping("/simple")
    public String simple() {
        return "/clients/simple";
    }
}
