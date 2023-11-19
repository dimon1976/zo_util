package by.demon.zoom.controller;

import by.demon.zoom.domain.FileForm;
import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.service.impl.*;
import by.demon.zoom.util.DataDownload;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController {


    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
    //    private List<String> recentLabels = new ArrayList<>();
//    private String lastUploadLabel;
    private final Map<String, Object> processingServices = new HashMap<>();
    private final HttpServletResponse response;
    private final MegatopService megatopService;

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
            HttpServletResponse response, MegatopService megatopService1) {
        this.megatopService = megatopService1;
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
    public @ResponseBody String getUrl(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFiles("getUrl", multipartFile);
    }

    @PostMapping("/stat/")
    public @ResponseBody String editStatisticFile(@RequestParam("file") MultipartFile[] multipartFile,
                                                  @RequestParam(value = "showSource", required = false) String showSource,
                                                  @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                                  @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                                  @RequestParam(value = "showDateAdd", required = false) String showDateAdd) {
        return processFiles("stat", multipartFile, showSource, sourceReplace, showCompetitorUrl, showDateAdd);
    }

    @PostMapping("/vlook")
    public @ResponseBody String excelVlook(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFiles("vlook", multipartFile);
    }

    //    @PostMapping("/megatop/upload")
//    public @ResponseBody String uploadMegatop(@RequestParam("file") MultipartFile[] multipartFile,
//                                             @RequestParam(value = "uploadLabel", defaultValue = "") String uploadLabel,
//                                             RedirectAttributes redirectAttributes,
//                                             Model model) {
//        ArrayList<File> files = new ArrayList<>();
//        if (multipartFile != null) {
//            for (MultipartFile file : multipartFile) {
//                String filePath = saveFileAndGetPath(file);
//                File transferTo = new File(filePath);
//                files.add(transferTo);
//            }
//        }
//        return megatopService.export(files, uploadLabel);
////        return processFiles("megatop", multipartFile, uploadLabel);
//    }
    @PostMapping("/megatop")
    public String handleFileUpload(@ModelAttribute FileForm fileForm) throws IOException {
        String label = fileForm.getLabel();
        ArrayList<File> files = new ArrayList<>();
        if (fileForm.getFiles() != null) {
            for (MultipartFile file : fileForm.getFiles()) {
                String filePath = saveFileAndGetPath(file);
                File transferTo = new File(filePath);
                files.add(transferTo);
            }
        }
        // Обработка файлов и сохранение в базу данных
        megatopService.export(files, label);

        // Перенаправление на страницу с выбором метки для выгрузки
//        ModelAndView modelAndView = new ModelAndView("redirect:/excel/megatop");
//        modelAndView.addObject("labels", megatopService.getLatestLabels());
//        modelAndView.addObject("selectedLabel", label);

        return "index";
    }

    @PostMapping("/simpleReport")
    public @ResponseBody String excelSimpleReport(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFiles("simple", multipartFile);
    }


    @PostMapping("/edadeal")
    public @ResponseBody String excelEdadeal(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFiles("edadeal", multipartFile);
    }

    @PostMapping("/lenta")
    public @ResponseBody String excelLentaTask(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFiles("lenta", multipartFile);
    }


    @PostMapping("/lentaReport")
    public @ResponseBody String excelLentaReport(@ModelAttribute("lenta") Lenta lenta
            , @RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = saveFileAndGetPath(multipartFile);
            try {
                multipartFile.transferTo(new File(filePath));
                LentaService lentaService = new LentaService(new DataDownload());
                return lentaService.exportReport(filePath, new File(filePath), response, lenta.getAfterDate());
            } catch (IllegalStateException | IOException e) {
                log.error("Error while uploading file: {}", e.getMessage());
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "/clients/lenta";
    }


    private String processFiles(String action, MultipartFile[] multipartFile, String... additionalParams) {
        FileProcessingService processingService = (FileProcessingService) processingServices.get(action);
        if (processingService == null) {
            log.warn("Unsupported action: {}", action);
            return ("Unsupported action: {}" + action);
        }
        for (MultipartFile file : multipartFile) {
            if (ifExist(file)) {
                String processSingleFile = processSingleFile(additionalParams, file, processingService);
                String filePath = getFilePath(file);
                cleanupTempFile(new File(filePath));
                if (processSingleFile != null) return processSingleFile;
            }
        }
        return "index";
    }

    @Nullable
    private String processSingleFile(String[] additionalParams, MultipartFile file, FileProcessingService processingService) {
        String filePath = saveFileAndGetPath(file);
        // Создаем директорию, если она не существует
        createTempDirectory();
        try {
            processingService.export(filePath, new File(filePath), response, additionalParams);
        } catch (IllegalStateException | IOException e) {
            log.error("Error while uploading file: {}", e.getMessage());
            // Если обработка не удалась, файл остается на месте
            return "File uploaded failed: " + getOrgName(file);
        }
        return null;
    }

    private String saveFileAndGetPath(MultipartFile file) {
        try {
            String filePath = getFilePath(file);
            File transferTo = new File(filePath);
            createTempDirectory();

            try (OutputStream os = new FileOutputStream(transferTo)) {
                os.write(file.getBytes());
                log.info("File uploaded successfully: {}", getOrgName(file));
                return filePath;
            } catch (IOException e) {
                log.error("Error saving file: {}", e.getMessage());
                throw new IOException("Error saving file", e);
            }
        } catch (IOException e) {
            log.error("Error creating file path: {}", e.getMessage());
            throw new RuntimeException("Error creating file path", e);
        }
    }


    private void createTempDirectory() {
        if (TEMP_PATH == null) {
            log.error("TEMP_PATH is null. Unable to create directory.");
            return;
        }

        File directory = new File(TEMP_PATH);
        if (!directory.exists() && !directory.mkdirs()) {
            log.error("Failed to create directory: {}", TEMP_PATH);
        } else {
            log.info("Directory created successfully: {}", TEMP_PATH);
        }
    }

    private void cleanupTempFile(File transferTo) {
        if (transferTo.delete()) {
            log.info("File removed successfully: {}", transferTo.getName());
        } else {
            log.error("Failed to remove file: {}", transferTo.getName());
        }
    }

    private boolean ifExist(MultipartFile multipartFile) {
        return multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty();
    }

    private String getFilePath(MultipartFile multipartFile) {
        String orgName = getOrgName(multipartFile);
        String extension = getExtension(orgName);
        return TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension);
    }

    private String getExtension(String orgName) {
        return orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
    }

    private String getOrgName(MultipartFile multipartFile) {
        return multipartFile.getOriginalFilename();
    }

    private String generateUniqueLabel() {
        String datePart = DATE_FORMATTER.format(new Date());
        String uniquePart = String.format("%09d", ThreadLocalRandom.current().nextLong(1000000000L));
        return datePart + uniquePart;
    }
}

