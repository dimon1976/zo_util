package by.demon.zoom.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FileForm {

    private List<MultipartFile> files;
    private String label;
}
