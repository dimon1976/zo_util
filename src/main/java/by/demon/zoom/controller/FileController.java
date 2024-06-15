package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.domain.av.AvHandbook;
import by.demon.zoom.domain.imp.av.AvDataEntity;
import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import by.demon.zoom.dto.imp.MegatopDTO;
import by.demon.zoom.dto.imp.SimpleDTO;
import by.demon.zoom.dto.imp.UrlDTO;
import by.demon.zoom.dto.imp.VlookBarDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import by.demon.zoom.dto.lenta.LentaTaskDTO;
import by.demon.zoom.service.impl.*;
import by.demon.zoom.service.impl.av.AvHandbookService;
import by.demon.zoom.service.impl.av.AvReportService;
import by.demon.zoom.service.impl.av.AvTaskService;
import by.demon.zoom.service.impl.lenta.EdadealService;
import by.demon.zoom.service.impl.lenta.LentaReportService;
import by.demon.zoom.service.impl.lenta.LentaTaskService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController {

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
    public @ResponseBody String getUrl(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ArrayList<UrlDTO> urlDTOS = urlService.readFiles(getFiles(multipartFile));
        urlService.download(urlDTOS, response, "excel");
        return "";
    }

    @PostMapping("/stat/")
    public @ResponseBody String uploadStatisticFile(@RequestParam("file") MultipartFile[] multipartFile,
                                                    @RequestParam(value = "showSource", required = false) String showSource,
                                                    @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                                    @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                                    @RequestParam(value = "showDateAdd", required = false) String showDateAdd) throws IOException {
        String[] additionalParam = new String[]{showSource, sourceReplace, showCompetitorUrl, showDateAdd};
        ArrayList<List<Object>> lists = statisticService.readFiles(getFiles(multipartFile), additionalParam);
        statisticService.download(lists, response, "excel", additionalParam);
        return "";
    }

    @PostMapping("/vlook")
    public @ResponseBody String uploadVlook(@RequestParam("file") MultipartFile[] multipartFile,
                                            HttpServletResponse response,
                                            @RequestParam(value = "format", required = false) String format) throws IOException {
        ArrayList<VlookBarDTO> vlookBarDTOS = vlookService.readFiles(getFiles(multipartFile));
        vlookService.download(vlookBarDTOS, response, "csv");
        return "";
    }

    @PostMapping("/megatop/upload")
    public @ResponseBody ModelAndView megatopFileUpload(
            @RequestParam("file") MultipartFile[] multipartFile,
            @RequestParam(value = "label", required = false) String label,
            Model model) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            String[] additionalParam = new String[]{label};
            megatopService.readFiles(getFiles(multipartFile), additionalParam);
            model.addAttribute("status", "success");
            model.addAttribute("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            model.addAttribute("status", "error");
            model.addAttribute("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }


    @PostMapping("/megatop/download")
    public @ResponseBody String megatopDownload(@RequestParam(value = "downloadLabel", required = false) String label,
                                                @RequestParam(value = "format", required = false) String format,
                                                HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{label};
        ArrayList<MegatopDTO> megatopDTOS = megatopService.getDto(additionalParam);
        megatopService.download(megatopDTOS, response, format);
        return "";
    }

    @PostMapping("/simple/upload/report")
    public @ResponseBody ModelAndView uploadSimpleReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            List<File> files = getFiles(multipartFile);
            ArrayList<SimpleDTO> simpleDTOS = simpleService.readFiles(files);
            String[] additionalParam = new String[]{files.get(0).getName()};
            simpleService.download(simpleDTOS, response, "excel", additionalParam);
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/av/upload/task")
    public @ResponseBody ModelAndView uploadAvTask(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            avTaskService.readFiles(getFiles(multipartFile));
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/av/upload/report")
    public @ResponseBody ModelAndView uploadAvReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            avReportService.readFiles(getFiles(multipartFile));
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/av/upload/handbook")
    public @ResponseBody ModelAndView uploadAvHandbook(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            ArrayList<AvHandbook> handbooks = handbookService.readFiles(getFiles(multipartFile));
            handbookService.save(handbooks);
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/av/download/report")
    public @ResponseBody String avDownloadReport(@RequestParam(value = "report_no", required = false) String report_no,
                                                 @RequestParam(value = "format", required = false) String format,
                                                 @RequestParam(value = "delete", required = false) String delete,
                                                 HttpServletResponse response) throws IOException {
        if (delete != null) {
            avReportService.deleteReport(report_no);
            return "";
        } else {
            String[] additionalParam = new String[]{report_no};
            ArrayList<CsvAvReportEntity> avDataEntityArrayList = avReportService.getDto(additionalParam);
            avReportService.download(avDataEntityArrayList, response, "csv");
            return "";
        }
    }

    @PostMapping("av/download/task")
    public @ResponseBody String avDownloadTask(@RequestParam(value = "task_no", required = false) String task_no,
                                               @RequestParam(value = "retailNetworkCode", required = false) String retailNetworkCode,
                                               @RequestParam(value = "format", required = false) String format,
                                               @RequestParam(value = "delete", required = false) String delete,
                                               HttpServletResponse response) throws IOException {
        if (delete != null) {
            avTaskService.deleteTask(task_no);
            return "";
        } else {
            String[] additionalParam = new String[]{task_no, retailNetworkCode, format};
            ArrayList<AvDataEntity> avDataEntityArrayList = avTaskService.getDto(additionalParam);
            avTaskService.download(avDataEntityArrayList, response, "csv", additionalParam);
            return "";
        }
    }

    @PostMapping("/lenta/upload/edadeal")
    public @ResponseBody ModelAndView uploadEdadeal(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            ArrayList<List<Object>> list = edadealService.readFiles(getFiles(multipartFile));
            edadealService.download(list, response, "excel");
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/lenta/upload/task")
    public @ResponseBody ModelAndView uploadLentaTask(@RequestParam("file") MultipartFile[] multipartFile,
                                                @RequestParam(value = "lenta", required = false) String lenta) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            ArrayList<LentaTaskDTO> collection = lentaTaskService.readFiles(getFiles(multipartFile));
            lentaTaskService.download(collection, response, "excel");
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }


    @PostMapping("/lenta/upload/report")
    public @ResponseBody ModelAndView uploadLentaReport(@ModelAttribute("lenta") Lenta lenta,
                                                  @RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(lenta.getAfterDate());
            String[] additionalParam = new String[]{"report", date};
            ArrayList<LentaReportDTO> reportCollection = lentaReportService.readFiles(getFiles(multipartFile), additionalParam);
            lentaReportService.download(reportCollection, response, "excel");
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @NotNull
    private List<File> getFiles(MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .filter(this::ifExist)
                .map(this::saveFileAndGetPath)
                .filter(Objects::nonNull)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    private Path saveFileAndGetPath (MultipartFile file) {
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

