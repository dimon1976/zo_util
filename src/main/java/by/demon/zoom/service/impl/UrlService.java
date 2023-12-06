package by.demon.zoom.service.impl;

import by.demon.zoom.dto.UrlDTO;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlService implements FileProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(UrlService.class);
    private static final List<String> HEADER = List.of("ID", "Ссылка конкурент");
    private final DataDownload dataDownload;
    private final DataToExcel<UrlDTO> dataToExcel;

    public UrlService(DataDownload dataDownload, DataToExcel<UrlDTO> dataToExcel) {
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
    }


    public Collection readFiles(List<File> files, String... additionalParams) throws IOException {
        LOG.info("Exporting data...");

//        try {
//            List<List<Object>> excelData = readDataFromFile(path.toFile());
//            Collection<UrlDTO> urlDTOList = getUrlDTOList(excelData);
//
//            try (OutputStream out = Files.newOutputStream(path)) {
//                short skipLines = 0;
//                dataToExcel.exportToExcel(HEADER, urlDTOList, out, skipLines);
////                dataDownload.download(file.getName(), filePath, response);
//            }
//
//            LOG.info("Data exported successfully");
//            return "export successful";
//        } catch (IOException e) {
//            LOG.error("Error exporting data: {}", e.getMessage());
//            return "Error exporting data";
//        }
        return null;
    }

    @Override
    public void download(HttpServletResponse response,Path path,  String format, String... additionalParams) throws IOException {

    }

    @Override
    public void save(Collection<T> collection) {

    }

    @Override
    public Collection<T> listAll() {
        return null;
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


