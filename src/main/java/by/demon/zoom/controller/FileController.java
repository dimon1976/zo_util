package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.*;
import by.demon.zoom.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController {

    private final Map<String, Object> processingServices = new HashMap<>();
    private final HttpServletResponse response;

    @Value("${temp.path}")
    private String TEMP_PATH;

    public FileController(
            StatisticService statisticService,
            VlookService vlookService,
            MegatopService megatopService,
            LentaService lentaService,
            SimpleService simpleService,
            UrlService urlService,
            EdadealService edadealService,
            HttpServletResponse response) {
        this.processingServices.put("stat", statisticService);
        this.processingServices.put("vlook", vlookService);
        this.processingServices.put("megatop", megatopService);
        this.processingServices.put("lenta", lentaService);
        this.processingServices.put("simple", simpleService);
        this.processingServices.put("getUrl", urlService);
        this.processingServices.put("edadeal", edadealService);
        this.response = response;
    }

    @PostMapping("/getUrl/")
    public @ResponseBody String getUrl(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("getUrl", multipartFile);
    }

    @PostMapping("/stat/")
    public @ResponseBody String editStatisticFile(@RequestParam("file") MultipartFile multipartFile,
                                                  @RequestParam(value = "showSource", required = false) String showSource,
                                                  @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                                  @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                                  @RequestParam(value = "showDateAdd", required = false) String showDateAdd) {
        return processFile("stat", multipartFile, showSource, sourceReplace, showCompetitorUrl, showDateAdd);
    }

    @PostMapping("/vlook")
    public @ResponseBody String excelVlook(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("vlook", multipartFile);
    }

    @PostMapping("/megatop")
    public @ResponseBody String excelMegatop(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("megatop", multipartFile);
    }

    @PostMapping("/simpleReport")
    public @ResponseBody String excelSimpleReport(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("simple", multipartFile);
    }


    @PostMapping("/edadeal")
    public @ResponseBody String excelEdadeal(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("edadeal", multipartFile);
    }

    @PostMapping("/lenta")
    public @ResponseBody String excelLentaTask(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("lenta", multipartFile);
    }
    @PostMapping("/urlFrom")
    public @ResponseBody String excelUrlFrom(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("urlFrom", multipartFile);
    }

    @PostMapping("/urlTo")
    public @ResponseBody String excelUrlTo(@RequestParam("file") MultipartFile multipartFile) {
        return processFile("urlTo", multipartFile);
    }

    @PostMapping("/deleteAll")
    public @ResponseBody String excelDeleteAll() {
        Object deleteAll = processingServices.get("vlook");
        return ((FileProcessingService) deleteAll).deleteAll();
    }


    @PostMapping("/lentaReport")
    public @ResponseBody String excelLentaReport(@ModelAttribute("lenta") Lenta lenta
            , @RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                LentaService lentaService = new LentaService();
                return lentaService.exportReport(filePath, transferTo, response, lenta.getAfterDate());
            } catch (IllegalStateException | IOException e) {
                log.error("Error while uploading file: {}", e.getMessage());
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "/clients/lenta";
    }


    private String processFile(String action, MultipartFile multipartFile, String... additionalParams) {
        Object processingService = processingServices.get(action);
        String result;
//&& processingService instanceof FileProcessingService
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);

            // Создаем директорию, если она не существует
            File directory = new File(TEMP_PATH);
            if (!directory.exists() && !directory.mkdirs()) {
                // Если директория не создалась и возвращен false, можно обработать эту ситуацию
                log.error("Failed to create directory: {}", TEMP_PATH);
            }
            try {
                multipartFile.transferTo(transferTo);
                log.info("File uploaded successfully: {}", getOrgName(multipartFile));
                if (action.equals("urlFrom")){
                    Object urlFrom = processingServices.get("vlook");
                    result = ((FileProcessingService) urlFrom).saveAll(filePath, transferTo, response, action);
                } else if (action.equals("urlTo")) {
                    Object urlTo = processingServices.get("vlook");
                    result = ((FileProcessingService) urlTo).saveAll(filePath, transferTo, response, action);
                } else {
                    result = ((FileProcessingService) processingService).export(filePath, transferTo, response, additionalParams);
                }

                // Проверяем, существует ли папка
                if (directory.exists()) {
                    // Удаляем временный файл после обработки
                    if (transferTo.delete()) {
                        log.info("File removed successfully: {}", getOrgName(multipartFile));
                    } else {
                        log.error("Failed to remove file: {}", getOrgName(multipartFile));
                    }
                }
                return result;
            } catch (IllegalStateException | IOException e) {
                log.error("Error while uploading file: {}", e.getMessage());
                // Если обработка не удалась, файл остается на месте
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "index";
    }


    private boolean ifExist(MultipartFile multipartFile) {
        return multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty();
    }

    private String getFilePath(MultipartFile multipartFile) {
        String orgName = getOrgName(multipartFile);
        assert orgName != null;
        String extension = getExtension(orgName);
        return TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension);
    }

    private String getExtension(String orgName) {
        return orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
    }

    private String getOrgName(MultipartFile multipartFile) {
        return multipartFile.getOriginalFilename();
    }
}

