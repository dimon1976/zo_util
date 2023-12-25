package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.domain.av.Handbook;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.dto.UrlDTO;
import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.dto.lenta.LentaTaskDTO;
import by.demon.zoom.service.impl.*;
import by.demon.zoom.service.impl.av.AvHandbookService;
import by.demon.zoom.service.impl.av.AvReportService;
import by.demon.zoom.service.impl.av.AvTaskService;
import by.demon.zoom.service.impl.lenta.EdadealService;
import by.demon.zoom.service.impl.lenta.LentaReportService;
import by.demon.zoom.service.impl.lenta.LentaTaskService;
import by.demon.zoom.util.DataDownload;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    private final HttpServletResponse response;
    private final UrlService urlService;
    private final StatisticService statisticService;
    private final VlookService vlookService;
    private final MegatopService megatopService;
    private final LentaReportService lentaReportService;
    private final LentaTaskService lentaTaskService;
    private final SimpleService simpleService;
    private final EdadealService edadealService;
    private final AvReportService avReportService;
    private final AvTaskService avTaskService;
    private final AvHandbookService handbookService;


    @Value("${temp.path}")
    private String TEMP_PATH;

    public FileController(HttpServletResponse response, UrlService urlService, StatisticService statisticService, VlookService vlookService, MegatopService megatopService, LentaReportService lentaReportService, LentaTaskService lentaTaskService, SimpleService simpleService, EdadealService edadealService, AvReportService avReportService, AvTaskService avTaskService, AvHandbookService handbookService) {
        this.response = response;
        this.urlService = urlService;
        this.statisticService = statisticService;
        this.vlookService = vlookService;
        this.megatopService = megatopService;
        this.lentaReportService = lentaReportService;
        this.lentaTaskService = lentaTaskService;
        this.simpleService = simpleService;
        this.edadealService = edadealService;
        this.avReportService = avReportService;
        this.avTaskService = avTaskService;
        this.handbookService = handbookService;
    }


    @PostMapping("/getUrl/")
    public @ResponseBody String getUrl(@RequestParam("file") MultipartFile[] multipartFile) {
        ArrayList<UrlDTO> urlDTOS = urlService.readFiles(getFiles(multipartFile));
        return "";
    }

    @PostMapping("/stat/")
    public @ResponseBody String uploadStatisticFile(@RequestParam("file") MultipartFile[] multipartFile,
                                                    @RequestParam(value = "showSource", required = false) String showSource,
                                                    @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                                    @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                                    @RequestParam(value = "showDateAdd", required = false) String showDateAdd) {
        String[] additionalParam = new String[]{showSource, sourceReplace, showCompetitorUrl, showDateAdd};
        ArrayList<List<Object>> lists = statisticService.readFiles(getFiles(multipartFile), additionalParam);
        return "";
    }

    @PostMapping("/vlook")
    public @ResponseBody String uploadVlook(@RequestParam("file") MultipartFile[] multipartFile,
                                            HttpServletResponse response,
                                            @RequestParam(value = "format", required = false) String format) {
        ArrayList<VlookBarDTO> vlookBarDTOS = vlookService.readFiles(getFiles(multipartFile));
        return "";
    }

    @PostMapping("/megatop/upload")
    public @ResponseBody String handleFileUpload(
            @RequestParam("file") MultipartFile[] multipartFile,
            @RequestParam(value = "label", required = false) String label) throws IOException {
        String[] additionalParam = new String[]{label};
        ArrayList<Megatop> megatop = megatopService.readFiles(getFiles(multipartFile), additionalParam);
        return "";
    }


    @PostMapping("/megatop/download")
    public @ResponseBody String downloadData(@RequestParam(value = "downloadLabel", required = false) String label,
                                             @RequestParam(value = "format", required = false) String format,
                                             HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{label};
        Path path = DataDownload.getPath("data", format);
        megatopService.download(response, path, format, additionalParam);
        return "";
    }

    @PostMapping("/simple/upload/report")
    public @ResponseBody String uploadSimpleReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ArrayList<SimpleDTO> simpleDTOS = simpleService.readFiles(getFiles(multipartFile));
        return "";
    }

    @PostMapping("/av/task")
    public @ResponseBody String uploadAvTask(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        avTaskService.readFiles(getFiles(multipartFile));
        return "";
    }

    @PostMapping("/av/report")
    public @ResponseBody String uploadAvReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        avReportService.readFiles(getFiles(multipartFile));
        return "";
    }

    @PostMapping("/av/handbook")
    public @ResponseBody String uploadAvHandbook(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ArrayList<Handbook> handbooks = handbookService.readFiles(getFiles(multipartFile));
        return handbookService.save(handbooks);
    }

    @PostMapping("/av/download")
    public @ResponseBody String avDownload(@RequestParam(value = "task_no", required = false) String task_no,
                                           @RequestParam(value = "report_no", required = false) String report_no,
                                           @RequestParam(value = "format", required = false) String format,
                                           @RequestParam(value = "retailerNetwork", required = false) String retailerNetwork,
                                           HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{task_no, report_no, retailerNetwork};
        return "";
    }

    @PostMapping("/lenta/upload/edadeal")
    public @ResponseBody String uploadEdadeal(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        edadealService.readFiles(getFiles(multipartFile));
        return "";
    }

    @PostMapping("/lenta/upload/task")
    public @ResponseBody String uploadLentaTask(@RequestParam("file") MultipartFile[] multipartFile,
                                                @RequestParam(value = "lenta", required = false) String lenta) throws IOException {
        Collection<LentaTaskDTO> collection = lentaTaskService.readFiles(getFiles(multipartFile));
        return "";
    }


    @PostMapping("/lenta/upload/report")
    public @ResponseBody String uploadLentaReport(@ModelAttribute("lenta") Lenta lenta,
                                                  @RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(lenta.getAfterDate());
        String[] additionalParam = new String[]{"report", date};
        Collection<LentaReportDTO> reportCollection = lentaReportService.readFiles(getFiles(multipartFile), additionalParam);
        return "/clients/lenta";
    }


//    private Collection<?> readFiles(String action, MultipartFile[] multipartFiles, String... additionalParams) throws IOException {
//        FileProcessingService<?> processingService = (FileProcessingService<?>) processingServices.get(action);
//        if (processingService == null) {
//            log.warn("Unsupported action: {}", action);
//            throw new IllegalArgumentException("Unsupported action: " + action);
//        }
//        List<File> files = getFiles(multipartFiles);
//        Collection<?> resultCollection = processingService.readFiles(files, additionalParams);
//
//        if (resultCollection != null) {
//            // Дальнейшая обработка в соответствии с типом T
//            return resultCollection;
//        } else {
//            throw new IllegalStateException("Invalid result type");
//        }
//    }

    @NotNull
    private List<File> getFiles(MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .filter(this::ifExist)
                .map(this::saveFileAndGetPath)
                .filter(Objects::nonNull)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

//    private String download(String action, HttpServletResponse response, String format, Object collection, String... additionalParams) throws IOException {
//        FileProcessingService<?> processingService = (FileProcessingService<?>) processingServices.get(action);
//        String processSingleFile = "";
//        if (processingService == null) {
//            log.warn("Unsupported action: {}", action);
//            return ("Unsupported action: {}" + action);
//        } else {
//            Path path = DataDownload.getPath("data", format);
////            processingService.download(response, path, format, (Collection<T>) collection, additionalParams);
//        }
//        return processSingleFile;
//    }

//    private String save(String action, Collection<?> collection) {
//        FileProcessingService<?> processingService = (FileProcessingService<?>) processingServices.get(action);
//        if (processingService == null) {
//            log.warn("Unsupported action: {}", action);
//            throw new IllegalArgumentException("Unsupported action: " + action);
//        }
////        return processingService.save(collection);
//        return "";
//    }

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

