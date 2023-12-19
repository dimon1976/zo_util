package by.demon.zoom.service.impl;

import by.demon.zoom.dao.HandbookRepository;
import by.demon.zoom.domain.av.Handbook;
import by.demon.zoom.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class HandbookService implements FileProcessingService<Handbook> {


    private final static Logger log = LoggerFactory.getLogger(HandbookService.class);
    private final HandbookRepository handbookRepository;

    public HandbookService(HandbookRepository handbookRepository) {
        this.handbookRepository = handbookRepository;
    }


    public String readFiles(Path path, HttpServletResponse response, String... additionalParams) throws IOException {
        List<List<Object>> lists = readDataFromFile(path.toFile());
        Collection<Handbook> handbookArrayList = getObjectList(lists);
        handbookRepository.deleteAll();
        log.info("Successful clearing of the handbook table");
        handbookRepository.saveAll(handbookArrayList);

        log.info("File {} processed and saved successfully.", path.getFileName());
        return "File processed and saved successfully.";
    }

    @Override
    public Collection<Handbook> readFiles(List<File> files, String... additionalParams) throws IOException {
        Collection<Handbook> handbookList = new ArrayList<>();
        for (File file : files) {
            try {
                List<List<Object>> lists = readDataFromFile(file);
                Collection<Handbook> handbook = getObjectList(lists);
                handbookList.addAll(handbook);
                log.info("File {} successfully read", file.getName());

            } catch (IOException e) {
                log.error("Error reading data from file: {}", file.getAbsolutePath(), e);

            } catch (Exception e) {
                log.error("Error processing file: {}", file.getAbsolutePath(), e);

            } finally {
                if (file.exists()) {
                    if (!file.delete()) {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        return handbookList;
    }

    @Override
    public String save(Collection<Handbook> collection) {
            try {
                handbookRepository.deleteAll(handbookRepository.findAll());
                log.info("Successful clearing of the handbook table");
                handbookRepository.saveAll(collection);
                return ""; // or handle return value based on saveAll outcome
            } catch (Exception e) {
                log.error("Error saving handbooks", e);
                throw e;
            }
    }

    private Collection<Handbook> getObjectList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createObjectFromList)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Handbook createObjectFromList(List<Object> str) {
        Handbook handbook = new Handbook();
        handbook.setRegionCode(getStringValue(str, 0));
        handbook.setRetailNetwork(getStringValue(str, 1));
        handbook.setPhysicalAddress(getStringValue(str, 2));
        handbook.setPriceZoneCode(getStringValue(str, 3));
        handbook.setWebSite(getStringValue(str, 4));
        handbook.setRegionCode(getStringValue(str, 5));
        handbook.setRegionName(getStringValue(str, 6));

        return handbook;
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }


}
