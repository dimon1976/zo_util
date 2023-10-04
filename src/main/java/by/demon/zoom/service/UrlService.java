package by.demon.zoom.service;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.dto.UrlDTO;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class UrlService {

    private final List<String> header = Arrays.asList("ID", "Ссылка конкурент");
    private final ExcelUtil<UrlDTO> excelUtil;

    public UrlService(ExcelUtil<UrlDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }

    public String export(String filePath, File file, HttpServletResponse response) throws IOException {
        Path of = Path.of(filePath);
        List<List<Object>> lists = readExcel(file);
        Collection<UrlDTO> collect = getUrlDTOList(lists);
//        try (OutputStream out = Files.newOutputStream(of)) {
//            short skipLines = 1;
//            excelUtil.exportExcel(header, collect, out, skipLines);
//            excelUtil.download(file.getName(), filePath, response);
//        }


        return null;
    }

    private Collection<UrlDTO> getUrlDTOList(List<List<Object>> lists) {
        ArrayList<Object> listUrlDto = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            for (int j = 0; j < lists.get(i).size(); j++) {
                String cell = (String) lists.get(i).get(j);
                if (cell.startsWith("http://") || cell.startsWith("https://")) {

                }
            }


        }


        return null;
    }
}
