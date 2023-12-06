package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.service.impl.*;
import by.demon.zoom.util.DataDownload;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController {

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
            HttpServletResponse response,
            MegatopService megatopService1,
            AvService avService,
            HandbookService handbookService) {
        this.megatopService = megatopService1;
        this.processingServices.put("stat", statisticService);
        this.processingServices.put("vlook", vlookService);
        this.processingServices.put("megatop", megatopService);
        this.processingServices.put("lenta", lentaService);
        this.processingServices.put("simple", simpleService);
        this.processingServices.put("getUrl", urlService);
        this.processingServices.put("edadeal", edadealService);
        this.processingServices.put("avService", avService);
        this.processingServices.put("handbook", handbookService);
        this.response = response;
    }

    @PostMapping("/getUrl/")
    public @ResponseBody String getUrl(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("getUrl", multipartFile);
        return "";
    }

    @PostMapping("/stat/")
    public @ResponseBody String editStatisticFile(@RequestParam("file") MultipartFile[] multipartFile,
                                                  @RequestParam(value = "showSource", required = false) String showSource,
                                                  @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                                  @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                                  @RequestParam(value = "showDateAdd", required = false) String showDateAdd) {
        String[] additionalParam = new String[]{showSource, sourceReplace, showCompetitorUrl, showDateAdd};
//        return readFiles("stat", multipartFile, additionalParam);
        return "";
    }

    @PostMapping("/vlook")
    public @ResponseBody String excelVlook(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        Collection<T> collection = readFiles("vlook", multipartFile);

        return "";
    }

    @PostMapping("/megatop/upload")
    public @ResponseBody String handleFileUpload(
//            @ModelAttribute FileForm fileForm,
            @RequestParam("file") MultipartFile[] multipartFile,
            @RequestParam(value = "label", required = false) String label) throws IOException {

//        String label = fileForm.getLabel();
//        ArrayList<File> files = new ArrayList<>();
//        if (fileForm.getFiles() != null) {
//            for (MultipartFile file : fileForm.getFiles()) {
//                Path filePath = saveFileAndGetPath(file);
//                File transferTo = new File(filePath.toAbsolutePath().toString());
//                files.add(transferTo);
//            }
//        }
        // Обработка файлов и сохранение в базу данных
//        return megatopService.export(files, label);
        String[] additionalParam = new String[]{label};
        Collection<T> collection = readFiles("megatop", multipartFile, additionalParam);
        return "";
    }


    @PostMapping("/megatop/download")
    public @ResponseBody String downloadData(@RequestParam(value = "downloadLabel", required = false) String label,
                                             @RequestParam(value = "format", required = false) String format,
                                             HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{label};
        return download("megatop", response, format, additionalParam);
    }

    @PostMapping("/simpleReport")
    public @ResponseBody String excelSimpleReport(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("simple", multipartFile);
        return "";
    }

    @PostMapping("/av/task")
    public @ResponseBody String avTask(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("avService", multipartFile, "task");
        return "";
    }

    @PostMapping("/av/report")
    public @ResponseBody String avReport(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("avService", multipartFile, "report");
        return "";
    }

    @PostMapping("/av/handbook")
    public @ResponseBody String avHandbook(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("handbook", multipartFile);
        return "";
    }

    @PostMapping("/av/download")
    public @ResponseBody String avDownload(@RequestParam(value = "task_no", required = false) String task_no,
                                           @RequestParam(value = "report_no", required = false) String report_no,
                                           @RequestParam(value = "format", required = false) String format,
                                           @RequestParam(value = "retailerNetwork", required = false) String retailerNetwork,
                                           HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{task_no, report_no, retailerNetwork};
        return download("avService", response, format, additionalParam);
    }

    @PostMapping("/edadeal")
    public @ResponseBody String excelEdadeal(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("edadeal", multipartFile);
        return "";
    }

    @PostMapping("/lenta")
    public @ResponseBody String excelLentaTask(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("lenta", multipartFile);
        return "";
    }


    @PostMapping("/lentaReport")
    public @ResponseBody String excelLentaReport(@ModelAttribute("lenta") Lenta lenta,
                                                 @RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            Path filePath = saveFileAndGetPath(multipartFile);
            try {
                multipartFile.transferTo(new File(filePath.toAbsolutePath().toString()));
                LentaService lentaService = new LentaService(new DataDownload());
                return lentaService.exportReport(filePath.toAbsolutePath().toString(), new File(filePath.toAbsolutePath().toString()), response, lenta.getAfterDate());
            } catch (IllegalStateException | IOException e) {
                log.error("Error while uploading file: {}", e.getMessage());
            }
        }
        return "/clients/lenta";
    }


    private Collection<T> readFiles(String action, MultipartFile[] multipartFile, String... additionalParams) throws IOException {
        FileProcessingService processingService = (FileProcessingService) processingServices.get(action);
        if (processingService == null) {
            log.warn("Unsupported action: {}", action);
            return null;
        }
        ArrayList<File> files = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            if (ifExist(file)) {
                Path path = saveFileAndGetPath(file);
                files.add(path.toFile());
            }
        }
        return processingService.readFiles(files, additionalParams);
    }

//    @Nullable
//    private String processSingleFile(String[] additionalParams, MultipartFile file, FileProcessingService processingService) {
//        Path filePath = saveFileAndGetPath(file);
//        String processSingleFile;
//        // Создаем директорию, если она не существует
//        createTempDirectory();
//        try {
////            processSingleFile = processingService.readFiles(filePath, response, additionalParams);
//        } catch (IllegalStateException | IOException e) {
//            log.error("Error while uploading file: {}", e.getMessage());
//            // Если обработка не удалась, файл остается на месте
//            return "File uploaded failed: " + getOrgName(file);
//        }
//        return processSingleFile;
//    }

    private String download(String action, HttpServletResponse response, String format, String... additionalParams) throws IOException {
        FileProcessingService processingService = (FileProcessingService) processingServices.get(action);
        String processSingleFile = "";
        if (processingService == null) {
            log.warn("Unsupported action: {}", action);
            return ("Unsupported action: {}" + action);
        } else {
            Path path = DataDownload.getPath("data", format);
            processingService.download(response, path, format, additionalParams);
        }

        return processSingleFile;
    }

    private Path saveFileAndGetPath(MultipartFile file) {
        try {
            Path filePath = getFilePath(file);
            File transferTo = new File(filePath.toAbsolutePath().toString());
            createTempDirectory();

            try (OutputStream os = new FileOutputStream(transferTo)) {
                os.write(file.getBytes());
                log.info("File uploaded successfully: {}", file.getOriginalFilename());
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

    private Path getFilePath(MultipartFile multipartFile) {
        String orgName = multipartFile.getOriginalFilename();
        String extension = getExtension(orgName);
        return Path.of(TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension));
    }

    private String getExtension(String orgName) {
        return orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
    }

}

