package by.demon.zoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientsController {

    @GetMapping("/lenta")
    public String lenta() {
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
