package by.demon.zoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UtilController handles utility-related requests.
 */
@Controller
@RequestMapping("/util")
public class UtilController {

    /**
     * Displays the utility index page.
     *
     * @return The view name for the utility index page
     */
    @GetMapping("")
    public String util() {
        return "/util/index";
    }
}
