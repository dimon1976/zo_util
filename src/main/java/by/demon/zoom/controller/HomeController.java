package by.demon.zoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * HomeController handles requests to the home page.
 */
@Controller
@RequestMapping("/")
public class HomeController {

    /**
     * Displays the home page.
     *
     * @return The view name for the home page
     */
    @GetMapping("/")
    public String lenta() {
        return "index";
    }
}
