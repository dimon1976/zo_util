package by.demon.zoom.domain;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class FileForm {

    private List<MultipartFile> files;
//    private String label;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date afterDate;
    private String format;
}
