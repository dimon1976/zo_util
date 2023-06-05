package by.demon.zoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Controller
@RequestMapping("/clients")
public class ClientsController {



    @GetMapping("/lenta")
    public String lenta(Model model) {
        model.addAttribute("Date", new Date());
        model.addAttribute("localDateTime", LocalDateTime.now());
        model.addAttribute("localDate", LocalDate.now());
        model.addAttribute("timestamp", Instant.now());
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
