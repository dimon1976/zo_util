package by.demon.zoom.controller;

import by.demon.zoom.domain.av.AvHandbook;
import by.demon.zoom.domain.imp.av.AvDataEntity;
import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import by.demon.zoom.dto.imp.SimpleDTO;
import by.demon.zoom.dto.imp.UrlDTO;
import by.demon.zoom.dto.imp.VlookBarDTO;
import by.demon.zoom.service.impl.*;
import by.demon.zoom.service.impl.av.AvHandbookService;
import by.demon.zoom.service.impl.av.AvReportService;
import by.demon.zoom.service.impl.av.AvTaskService;
import by.demon.zoom.util.FileUploadHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController {

    private final UrlService urlService;
    private final StatisticService statisticService;
    private final VlookService vlookService;
    private final SimpleService simpleService;
    private final AvReportService avReportService;
    private final AvTaskService avTaskService;
    private final AvHandbookService handbookService;
    private final YandexUrlService yandexUrlService;

    public FileController(UrlService urlService, StatisticService statisticService, VlookService vlookService, SimpleService simpleService, AvReportService avReportService, AvTaskService avTaskService, AvHandbookService handbookService, YandexUrlService yandexUrlService) {
        this.urlService = urlService;
        this.statisticService = statisticService;
        this.vlookService = vlookService;
        this.simpleService = simpleService;
        this.avReportService = avReportService;
        this.avTaskService = avTaskService;
        this.handbookService = handbookService;
        this.yandexUrlService = yandexUrlService;
    }

    @PostMapping("/getUrl/")
    public void getUrl(@RequestParam("file") MultipartFile[] multipartFile, HttpServletResponse response) throws IOException {
        ArrayList<UrlDTO> urlDTOS = urlService.readFiles(FileUploadHandler.getFiles(multipartFile));
        urlService.download(urlDTOS, response, "excel");
    }

    @PostMapping("/stat/")
    public void uploadStatisticFile(@RequestParam("file") MultipartFile[] multipartFile,
                                    @RequestParam(value = "showSource", required = false) String showSource,
                                    @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                    @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                    @RequestParam(value = "showDateAdd", required = false) String showDateAdd,
                                    HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{showSource, sourceReplace, showCompetitorUrl, showDateAdd};
        ArrayList<List<Object>> lists = statisticService.readFiles(FileUploadHandler.getFiles(multipartFile), additionalParam);
        statisticService.download(lists, response, "excel", additionalParam);
    }

    @PostMapping("/vlook")
    public void uploadVlook(@RequestParam("file") MultipartFile[] multipartFile, HttpServletResponse response) throws IOException {
        ArrayList<VlookBarDTO> vlookBarDTOS = vlookService.readFiles(FileUploadHandler.getFiles(multipartFile));
        vlookService.download(vlookBarDTOS, response, "csv");
    }

    @PostMapping("/simple/upload/report")
    public void uploadSimpleReport(@RequestParam("file") MultipartFile[] multipartFile, HttpServletResponse response) throws IOException {
        List<File> files = FileUploadHandler.getFiles(multipartFile);
        ArrayList<SimpleDTO> simpleDTOS = simpleService.readFiles(files);
        String[] additionalParam = new String[]{files.get(0).getName()};
        simpleService.download(simpleDTOS, response, "excel", additionalParam);
    }

    @PostMapping("/av/upload/task")
    public ModelAndView uploadAvTask(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFileUpload(multipartFile, avTaskService::readFiles);
    }

    @PostMapping("/av/upload/report")
    public ModelAndView uploadAvReport(HttpServletRequest request, @RequestParam("file") MultipartFile[] multipartFile) {
        String cityId = request.getParameter("city");
        String typeReport = request.getParameter("typeReport");
        String[] additionalParam = new String[]{cityId, typeReport};
//        return processFileUpload(multipartFile, avReportService::readFiles);
        return processFileUpload(multipartFile, file -> {
            avReportService.readFiles(file, additionalParam);
        });
    }

    @PostMapping("/av/upload/handbook")
    public ModelAndView uploadAvHandbook(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFileUpload(multipartFile, files -> {
            ArrayList<AvHandbook> handbooks = handbookService.readFiles(files);
            handbookService.save(handbooks);
        });
    }

    @PostMapping("/av/download/report")
    public ModelAndView avDownloadReport(@RequestParam(value = "report_no", required = false) String report_no,
                                         @RequestParam(value = "format", required = false) String format,
                                         @RequestParam(value = "delete", required = false) String delete,
                                         HttpServletResponse response) throws IOException {
        ModelAndView modelAndView = new ModelAndView("deleteResult");
        if (delete != null) {
            int deleteCount = avReportService.deleteReport(report_no);
            modelAndView.addObject("deleteCount", deleteCount);
            return modelAndView;
        } else {
            String[] additionalParam = new String[]{report_no};
            ArrayList<CsvAvReportEntity> avDataEntityArrayList = avReportService.getDto(additionalParam);
            avReportService.download(avDataEntityArrayList, response, "csv");
            return null;
        }
    }

    @PostMapping("av/download/task")
    public @ResponseBody ModelAndView avDownloadTask(@RequestParam(value = "task_no", required = false) String task_no,
                                                     @RequestParam(value = "retailNetworkCode", required = false) String retailNetworkCode,
                                                     @RequestParam(value = "format", required = false) String format,
                                                     @RequestParam(value = "delete", required = false) String delete,
                                                     HttpServletResponse response) throws IOException {
        ModelAndView modelAndView = new ModelAndView("deleteResult");
        if (delete != null) {
            int deleteCount = avTaskService.deleteTask(task_no);
            modelAndView.addObject("deleteCount", deleteCount);
            return modelAndView;
        } else {
            String[] additionalParam = new String[]{task_no, retailNetworkCode, format};
            ArrayList<AvDataEntity> avDataEntityArrayList = avTaskService.getDto(additionalParam);
            avTaskService.download(avDataEntityArrayList, response, "csv", additionalParam);
            return null;
        }
    }

    @PostMapping("av/update-temp-table-index")
    public String updateTempTableIndex() {
        avTaskService.updateTempTableIndex();
        return "/clients/av";
    }

    @PostMapping("av/truncate-temp-table-data")
    public String truncateTempTableData() {
        avTaskService.truncateTempTableData();
        return "/clients/av";
    }

    @PostMapping("/yandexUrl/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] file, Model model, HttpServletResponse response) throws IOException {
        ArrayList<List<Object>> yandexUrls = yandexUrlService.readFiles(FileUploadHandler.getFiles(file));
        yandexUrlService.download(yandexUrls, response, "excel");
        return "/util/index";
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
