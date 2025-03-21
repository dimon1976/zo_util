package by.demon.zoom.controller;

import by.demon.zoom.domain.City;
import by.demon.zoom.domain.TypeReport;
import by.demon.zoom.service.impl.av.AvHandbookService;
import by.demon.zoom.service.impl.av.AvReportService;
import by.demon.zoom.service.impl.av.AvTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientsController {
    private static final Logger logger = LoggerFactory.getLogger(ClientsController.class);

    private final AvReportService avReportService;
    private final AvTaskService avTaskService;
    private final AvHandbookService avHandbookService;


    public ClientsController( AvReportService avReportService, AvTaskService avTaskService, AvHandbookService avHandbookService) {
        this.avReportService = avReportService;
        this.avTaskService = avTaskService;
        this.avHandbookService = avHandbookService;
    }
    /**
     * Handles HTTP GET requests to the /clients/simple endpoint.
     *
     * This method is responsible for executing the simple method and returning the view for the clients/simple page.
     *
     * @return the view name for the clients/simple page
     */
    @GetMapping("/clients/simple")
    public String simple() {
        logger.info("Executing simple method");
        return "/clients/simple";
    }
    /**
     * Handles HTTP GET requests to the /clients/av endpoint.
     * This method is responsible for retrieving and returning data for the AV clients page.
     * @param model the Spring Model object used to pass data to the view
     * @return the name of the view to render
     */
    @GetMapping("/clients/av")
    public String av(Model model) {
        logger.info("Executing av method");
        try {
            model.addAttribute("cities", City.values());
            model.addAttribute("typeReports", TypeReport.values());
            model.addAttribute("reports", avReportService.getLatestReport());
            model.addAttribute("tasks", avTaskService.getLatestTask());
            model.addAttribute("retailNetworkCode", avHandbookService.getRetailNetworkCode());
        } catch (Exception e) {
            logger.error("Error executing av method", e);
            model.addAttribute("error", "Failed to load av data");
        }
        return "clients/av";
    }
}
