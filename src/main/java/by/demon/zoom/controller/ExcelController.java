package by.demon.zoom.controller;


import by.demon.zoom.service.DetmirService;
import org.jetbrains.annotations.NotNull;
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

    @Autowired
    private DetmirService detmirService;

    @PostMapping("/stat/detmirStats")
    public String detmirStats(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse response) throws IOException {
        if (multipartFile != null && !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty()) {
            String orgName = multipartFile.getOriginalFilename();
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

    @NotNull
    public File getFile(MultipartFile file) {
        return new File(TEMP_PATH + "/" + file.getOriginalFilename());
    }
}
