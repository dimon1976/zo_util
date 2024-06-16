package by.demon.zoom.controller;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.domain.av.AvHandbook;
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
import by.demon.zoom.util.FileUploadHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
        ArrayList<UrlDTO> urlDTOS = urlService.readFiles(FileUploadHandler.getFiles(multipartFile));
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
        ArrayList<List<Object>> lists = statisticService.readFiles(FileUploadHandler.getFiles(multipartFile), additionalParam);
        statisticService.download(lists, response, "excel", additionalParam);
        return "";
    }

    @PostMapping("/vlook")
    public ModelAndView uploadVlook(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, files -> {
            ArrayList<VlookBarDTO> vlookBarDTOS = vlookService.readFiles(files);
            vlookService.download(vlookBarDTOS, response, "csv");
        });
    }

    @PostMapping("/megatop/upload")
    public ModelAndView megatopFileUpload(@RequestParam("file") MultipartFile[] multipartFile,
                                          @RequestParam(value = "label", required = false) String label) throws IOException {
        return processFileUpload(multipartFile, files -> {
            String[] additionalParam = new String[]{label};
            megatopService.readFiles(files, additionalParam);
        });
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
    public ModelAndView uploadSimpleReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, files -> {
            ArrayList<SimpleDTO> simpleDTOS = simpleService.readFiles(files);
            String[] additionalParam = new String[]{files.get(0).getName()};
            simpleService.download(simpleDTOS, response, "excel", additionalParam);
        });
    }

    @PostMapping("/av/upload/task")
    public ModelAndView uploadAvTask(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, avTaskService::readFiles);
    }

    @PostMapping("/av/upload/report")
    public ModelAndView uploadAvReport(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, avReportService::readFiles);
    }

    @PostMapping("/av/upload/handbook")
    public ModelAndView uploadAvHandbook(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, files -> {
            ArrayList<AvHandbook> handbooks = handbookService.readFiles(files);
            handbookService.save(handbooks);
        });
    }

    @PostMapping("/lenta/upload/edadeal")
    public ModelAndView uploadEdadeal(@RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, files -> {
            ArrayList<List<Object>> list = edadealService.readFiles(files);
            edadealService.download(list, response, "excel");
        });
    }

    @PostMapping("/lenta/upload/task")
    public ModelAndView uploadLentaTask(@RequestParam("file") MultipartFile[] multipartFile,
                                        @RequestParam(value = "lenta", required = false) String lenta) throws IOException {
        return processFileUpload(multipartFile, files -> {
            ArrayList<LentaTaskDTO> collection = lentaTaskService.readFiles(files);
            lentaTaskService.download(collection, response, "excel");
        });
    }

    @PostMapping("/lenta/upload/report")
    public ModelAndView uploadLentaReport(@ModelAttribute("lenta") Lenta lenta,
                                          @RequestParam("file") MultipartFile[] multipartFile) throws IOException {
        return processFileUpload(multipartFile, files -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(lenta.getAfterDate());
            String[] additionalParam = new String[]{"report", date};
            ArrayList<LentaReportDTO> reportCollection = lentaReportService.readFiles(files, additionalParam);
            lentaReportService.download(reportCollection, response, "excel");
        });
    }

    private ModelAndView processFileUpload(MultipartFile[] multipartFiles, ConsumerWithIOException<List<File>> processFiles) {
        ModelAndView modelAndView = new ModelAndView("uploadStatus");
        try {
            List<File> files = FileUploadHandler.getFiles(multipartFiles);
            processFiles.accept(files);
            modelAndView.addObject("status", "success");
            modelAndView.addObject("message", "Files processed successfully");
        } catch (IOException | RuntimeException e) {
            log.error("Error processing files", e);
            modelAndView.addObject("status", "error");
            modelAndView.addObject("message", "Failed to process files: " + e.getMessage());
        }
        return modelAndView;
    }

    @FunctionalInterface
    interface ConsumerWithIOException<T> {
        void accept(T t) throws IOException;
    }
}
