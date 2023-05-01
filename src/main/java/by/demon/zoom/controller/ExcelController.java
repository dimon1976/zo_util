package by.demon.zoom.controller;


import by.demon.zoom.service.DetmirService;
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

import static by.demon.zoom.util.DateUtils.getDateTimeNow;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    @Value("${temp.path}")
    private String TEMP_PATH;

    public ExcelController(DetmirService detmirService, VlookService vlookService) {
        this.detmirService = detmirService;
        this.vlookService = vlookService;
    }

//    @Autowired
    private final DetmirService detmirService;
//    @Autowired
    private final VlookService vlookService;

    @PostMapping("/stat/detmirStats")
    public String detmirStats(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse response) throws IOException {
        if (multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty()) {
            String orgName = getOrgName(multipartFile);
            String extension = orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
            String filePath = TEMP_PATH + "/" + orgName.replace("." + extension, "-" + getDateTimeNow()) + "." + extension;
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return detmirService.getListWb(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + orgName;
            }
        }
        return "index";
    }

    @PostMapping("/vlook")
    public String excelVlook(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse response) throws IOException {
        if (multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty()) {
            String orgName = getOrgName(multipartFile);
            String extension = orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
            String filePath = TEMP_PATH + "/" + orgName.replace("." + extension, "-" + getDateTimeNow()) + "." + extension;
            File transferTo = new File(filePath);
            try {
                multipartFile.transferTo(transferTo);
                return vlookService.vlookBar(filePath, transferTo, response);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                return "File uploaded failed: " + orgName;
            }
        }
        return "index";
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
