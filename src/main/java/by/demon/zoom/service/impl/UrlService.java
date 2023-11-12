package by.demon.zoom.service.impl;

import by.demon.zoom.dto.UrlDTO;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.ExcelUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.ExcelUtil.readExcel;

@Service
public class UrlService implements FileProcessingService {

    private final List<String> header = List.of("ID", "Ссылка конкурент");
    private final ExcelUtil<UrlDTO> excelUtil;

    public UrlService(ExcelUtil<UrlDTO> excelUtil) {
        this.excelUtil = excelUtil;
    }

    public String export(String filePath, File file, HttpServletResponse response, String... additionalParams) throws IOException {
        List<List<Object>> excelData = readExcel(file);
        Collection<UrlDTO> urlDTOList = getUrlDTOList(excelData);
        Path path = Path.of(filePath);
        try (OutputStream out = Files.newOutputStream(path)) {
            short skipLines = 0;
            excelUtil.exportExcel(header, urlDTOList, out, skipLines);
            excelUtil.download(file.getName(), filePath, response);
        }
        return "export successful";
    }

    private Collection<UrlDTO> getUrlDTOList(List<List<Object>> excelData) {
        return excelData.stream()
                .flatMap(row -> row.stream()
                        .filter(cell -> cell instanceof String)
                        .map(cell -> (String) cell)
                        .filter(cell -> cell.startsWith("http://") || cell.startsWith("https://"))
                        .map(cell -> new UrlDTO(row.get(0).toString(), cell)))
                .collect(Collectors.toList());
    }
}

