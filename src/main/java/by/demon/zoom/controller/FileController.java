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
import by.demon.zoom.util.FileUploadHandler;
import lombok.extern.slf4j.Slf4j;
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

    public FileController(UrlService urlService, StatisticService statisticService, VlookService vlookService, MegatopService megatopService, LentaReportService lentaReportService, LentaTaskService lentaTaskService, SimpleService simpleService, EdadealService edadealService, AvReportService avReportService, AvTaskService avTaskService, AvHandbookService handbookService) {
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

    @PostMapping("/megatop/upload")
    public ModelAndView megatopFileUpload(@RequestParam("file") MultipartFile[] multipartFile,
                                          @RequestParam(value = "label", required = false) String label) {
        return processFileUpload(multipartFile, files -> {
            String[] additionalParam = new String[]{label};
            megatopService.readFiles(files, additionalParam);
        });
    }

    @PostMapping("/megatop/download")
    public void megatopDownload(@RequestParam(value = "downloadLabel", required = false) String label,
                                @RequestParam(value = "format", required = false) String format,
                                HttpServletResponse response) throws IOException {
        String[] additionalParam = new String[]{label};
        ArrayList<MegatopDTO> megatopDTOS = megatopService.getDto(additionalParam);
        megatopService.download(megatopDTOS, response, format);
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
    public ModelAndView uploadAvReport(@RequestParam("file") MultipartFile[] multipartFile) {
        return processFileUpload(multipartFile, avReportService::readFiles);
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

    @PostMapping("/lenta/upload/edadeal")
    public void uploadEdadeal(@RequestParam("file") MultipartFile[] multipartFile, HttpServletResponse response) throws IOException {
        ArrayList<List<Object>> list = edadealService.readFiles(FileUploadHandler.getFiles(multipartFile));
        edadealService.download(list, response, "excel");
    }

    @PostMapping("/lenta/upload/task")
    public void uploadLentaTask(@RequestParam("file") MultipartFile[] multipartFile,
                                @RequestParam(value = "lenta", required = false) String lenta,
                                HttpServletResponse response) throws IOException {
        ArrayList<LentaTaskDTO> collection = lentaTaskService.readFiles(FileUploadHandler.getFiles(multipartFile));
        lentaTaskService.download(collection, response, "excel");
    }

    @PostMapping("/lenta/upload/report")
    public void uploadLentaReport(@ModelAttribute("lenta") Lenta lenta,
                                  @RequestParam("file") MultipartFile[] multipartFile,
                                  HttpServletResponse response) throws IOException {
        List<File> files = FileUploadHandler.getFiles(multipartFile);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(lenta.getAfterDate());
        String[] additionalParam = new String[]{"report", date};
        ArrayList<LentaReportDTO> reportCollection = lentaReportService.readFiles(files, additionalParam);
        lentaReportService.download(reportCollection, response, "excel");
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
