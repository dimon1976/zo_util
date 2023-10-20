package by.demon.zoom.controller;


import by.demon.zoom.domain.Lenta;
import by.demon.zoom.service.*;
import by.demon.zoom.util.CsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/excel")
public class FileController {
    private final StatisticService statisticService;
    private final VlookService vlookService;
    private final MegatopService megatopService;
    private final LentaService lentaService;
    private final SimpleService simpleService;
    private final UrlService urlService;
    private final EdadealService edadealService;

    @Value("${temp.path}")
    private String TEMP_PATH;

    private final HttpServletResponse response;

    public FileController(StatisticService statisticService, VlookService vlookService, MegatopService megatopService, LentaService lentaService, SimpleService simpleService, UrlService urlService, CsvUtil csvUtil, EdadealService edadealService, HttpServletResponse response) {
        this.statisticService = statisticService;
        this.vlookService = vlookService;
        this.megatopService = megatopService;
        this.lentaService = lentaService;
        this.simpleService = simpleService;
        this.urlService = urlService;
        this.edadealService = edadealService;
        this.response = response;
    }



    @PostMapping("/getUrl/")
    public String getUrl(@RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return urlService.export(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "index";
    }

    @PostMapping("/stat/")
    public String editStatisticFile(@RequestParam("file") MultipartFile multipartFile, @RequestParam(value = "showSource", required = false) String showSource,
                                    @RequestParam(value = "sourceReplace", required = false) String sourceReplace,
                                    @RequestParam(value = "showCompetitorUrl", required = false) String showCompetitorUrl,
                                    @RequestParam(value = "showDateAdd", required = false) String showDateAdd) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return statisticService.export(filePath, transferTo, response, showSource, sourceReplace, showCompetitorUrl, showDateAdd);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "index";
    }


    @PostMapping("/vlook")
    public String excelVlook(@RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return vlookService.export(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "index";
    }


    @PostMapping("/megatop")
    public String excelMegatop(@RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return megatopService.export(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "index";
    }
//edeadeal
@PostMapping("/edadeal")
public String excelEdadeal(@RequestParam("file") MultipartFile multipartFile) {
    if (ifExist(multipartFile)) {
        String filePath = getFilePath(multipartFile);
        File transferTo = new File(filePath);
        try {
            multipartFile.transferTo(transferTo);
            return edadealService.export(filePath, transferTo, response);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            return "File uploaded failed: " + getOrgName(multipartFile);
        }
    }
    return "/clients/lenta";
}

    @PostMapping("/lenta")
    public String excelLentaTask(@RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return lentaService.export(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "/clients/lenta";
    }

    @PostMapping("/lentaReport")
    public String excelLentaReport(@ModelAttribute("lenta") Lenta lenta
            , @RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return lentaService.exportReport(filePath, transferTo, response, lenta.getAfterDate());
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "/clients/lenta";
    }


    @PostMapping("/simpleReport")
    public String excelSimpleReport(@RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return simpleService.export(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + getOrgName(multipartFile);
            }
        }
        return "/clients/simple";
    }

    private boolean ifExist(@RequestParam("file") MultipartFile multipartFile) {
        return multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty();
    }

    @NotNull
    private String getFilePath(MultipartFile multipartFile) {
        String orgName = getOrgName(multipartFile);
        assert orgName != null;
        String extension = getExtension(orgName);
        return TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension);
    }

    @NotNull
    private String getExtension(String orgName) {
        return orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
    }

    @Nullable
    private String getOrgName(MultipartFile multipartFile) {
        return multipartFile.getOriginalFilename();
    }

    @NotNull
    public File getFile(MultipartFile file) {
        return new File(TEMP_PATH + "/" + file.getOriginalFilename());
    }
}
