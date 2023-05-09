package by.demon.zoom.controller;


import by.demon.zoom.service.DetmirService;
import by.demon.zoom.service.LentaService;
import by.demon.zoom.service.MegatopService;
import by.demon.zoom.service.VlookService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    private final DetmirService detmirService;
    private final VlookService vlookService;
    private final MegatopService megatopService;
    private final LentaService lentaService;

    @Value("${temp.path}")
    private String TEMP_PATH;
    @Autowired
    private HttpServletResponse response;

    public ExcelController(DetmirService detmirService, VlookService vlookService, MegatopService megatopService, LentaService lentaService) {
        this.detmirService = detmirService;
        this.vlookService = vlookService;
        this.megatopService = megatopService;
        this.lentaService = lentaService;
    }

    @PostMapping("/stat/detmirStats")
    public String detmirStats(@RequestParam("file") MultipartFile multipartFile) {
        if (ifExist(multipartFile)) {
            String filePath = getFilePath(multipartFile);
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return detmirService.export(filePath, transferTo, response);
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


    @PostMapping("/lenta")
    public String excelLenta(@RequestParam("file") MultipartFile multipartFile) {
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
        return "index";
    }

    private boolean ifExist(@RequestParam("file") MultipartFile multipartFile) {
        return multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty();
    }

    @NotNull
    private String getFilePath(MultipartFile multipartFile) {
        String orgName = getOrgName(multipartFile);
        assert orgName != null;
        String extension = getExtension(orgName);
//        return TEMP_PATH + "/" + orgName.replace("." + extension, "-" + getDateTimeNow()) + "." + extension;
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
