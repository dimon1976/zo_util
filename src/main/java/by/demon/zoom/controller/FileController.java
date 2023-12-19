package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.domain.av.Handbook;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.service.impl.*;
import by.demon.zoom.util.DataDownload;
import lombok.extern.slf4j.Slf4j;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController<T> {

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
            HttpServletResponse response,
            AvService avService,
            HandbookService handbookService) {
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
    public @ResponseBody String getUrl(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        Collection<?> collection = readFiles("getUrl", multipartFile);
        return "";
    }

    @PostMapping("/stat/")
    public @ResponseBody String uploadStatisticFile(@RequestParam("file") MultipartFile[] multipartFile,
                                                    @RequestParam(value = "showSource", required = false) String showSource,
                                                    @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                                    @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                                    @RequestParam(value = "showDateAdd", required = false) String showDateAdd) throws IOException {
        String[] additionalParam = new String[]{showSource, sourceReplace, showCompetitorUrl, showDateAdd};
        Collection<?> collection = readFiles("stat", multipartFile, additionalParam);
        return "";
    }

    @PostMapping("/vlook")
    public @ResponseBody String uploadVlook(@RequestParam("file") MultipartFile[] multipartFile,
                                            HttpServletResponse response,
                                            @RequestParam(value = "format", required = false) String format) throws IOException {
        Collection<?> collection = readFiles("vlook", multipartFile);
        download("vlook", response, format, collection);
        return "";
    }

    @PostMapping("/megatop/upload")
    public @ResponseBody String handleFileUpload(
            @RequestParam("file") MultipartFile[] multipartFile,
            @RequestParam(value = "label", required = false) String label) throws IOException {
        String[] additionalParam = new String[]{label};
        Collection<?> collection = readFiles("megatop", multipartFile, additionalParam);
        return "";
    }


    @PostMapping("/megatop/download")
    public @ResponseBody String downloadData(@RequestParam(value = "downloadLabel", required = false) String label,
                                             @RequestParam(value = "format", required = false) String format,
                                             HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{label};
        return download("megatop", response, format, additionalParam);
    }

    @PostMapping("/simple/upload/report")
    public @ResponseBody String uploadSimpleReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        Collection<?> collection = readFiles("simple", multipartFile);
        return "";
    }

    @PostMapping("/av/task")
    public @ResponseBody String uploadAvTask(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("avService", multipartFile, "task");
        return "";
    }

    @PostMapping("/av/report")
    public @ResponseBody String uploadAvReport(@RequestParam("file") MultipartFile[] multipartFile) {
//        return readFiles("avService", multipartFile, "report");
        return "";
    }

    @PostMapping("/av/handbook")
    public @ResponseBody String uploadAvHandbook(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        Collection<?> handbookList = readFiles("handbook", multipartFile);
        FileProcessingService<Handbook> processingService = (FileProcessingService<Handbook>) processingServices.get("handbook");
        processingService.save((Collection<Handbook>) handbookList);
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

    @PostMapping("/lenta/upload/edadeal")
    public @ResponseBody String uploadEdadeal(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        Collection<?> collection = readFiles("edadeal", multipartFile);
        return "";
    }

    @PostMapping("/lenta/upload/task")
    public @ResponseBody String uploadLentaTask(@RequestParam("file") MultipartFile[] multipartFile,
                                                @RequestParam(value = "lenta", required = false) String lenta) throws IOException {
        String[] additionalParam = new String[]{"task", lenta};
        Collection<?> collection = readFiles("lenta", multipartFile, additionalParam);
        return "";
    }


    @PostMapping("/lenta/upload/report")
    public @ResponseBody String uploadLentaReport(@ModelAttribute("lenta") Lenta lenta,
                                                  @RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(lenta.getAfterDate());
        String[] additionalParam = new String[]{"report", date};
        Collection<?> collection = readFiles("lenta", multipartFile, additionalParam);
        return "/clients/lenta";
    }


    private Collection<?> readFiles(String action, MultipartFile[] multipartFiles, String... additionalParams) throws IOException {
        FileProcessingService<?> processingService = (FileProcessingService<?>) processingServices.get(action);
        if (processingService == null) {
            log.warn("Unsupported action: {}", action);
            throw new IllegalArgumentException("Unsupported action: " + action);
        }
        List<File> files = Arrays.stream(multipartFiles)
                .filter(this::ifExist)
                .map(this::saveFileAndGetPath)
                .filter(Objects::nonNull)
                .map(Path::toFile)
                .collect(Collectors.toList());
        Collection<?> resultCollection = processingService.readFiles(files, additionalParams);

        if (resultCollection != null) {
            // Дальнейшая обработка в соответствии с типом T
            return resultCollection;
        } else {
            throw new IllegalStateException("Invalid result type");
        }
    }

    private String download(String action, HttpServletResponse response, String format, Object collection, String... additionalParams) throws IOException {
        FileProcessingService<?> processingService = (FileProcessingService<?>) processingServices.get(action);
        String processSingleFile = "";
        if (processingService == null) {
            log.warn("Unsupported action: {}", action);
            return ("Unsupported action: {}" + action);
        } else {
            Path path = DataDownload.getPath("data", format);
//            processingService.download(response, path, format, (Collection<T>) collection, additionalParams);
        }
        return processSingleFile;
    }

    private String save(String action, Collection<?> collection) {
        FileProcessingService<?> processingService = (FileProcessingService<?>) processingServices.get(action);
        if (processingService == null) {
            log.warn("Unsupported action: {}", action);
            throw new IllegalArgumentException("Unsupported action: " + action);
        }
//        return processingService.save(collection);
        return "";
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

    private boolean ifExist(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private Path getFilePath(MultipartFile multipartFile) {
        String orgName = multipartFile.getOriginalFilename();
        assert orgName != null;
        String extension = getExtension(orgName);
        return Path.of(TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension));
    }

    private String getExtension(String orgName) {
        return orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
    }

}

