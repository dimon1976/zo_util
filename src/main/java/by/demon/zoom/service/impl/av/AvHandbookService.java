package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvHandbookRepository;
import by.demon.zoom.domain.av.Handbook;
import by.demon.zoom.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class AvHandbookService implements FileProcessingService<Handbook> {


    private final static Logger log = LoggerFactory.getLogger(AvHandbookService.class);
    private final AvHandbookRepository handbookRepository;

    public AvHandbookService(AvHandbookRepository handbookRepository) {
        this.handbookRepository = handbookRepository;
    }


    @Override
    public ArrayList<Handbook> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<Handbook> handbookList = new ArrayList<>();
        for (File file : files) {
            try {
                List<List<Object>> lists = readDataFromFile(file);
                Files.delete(file.toPath());
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
    public String save(ArrayList<Handbook> collection) {
        try {
            // Оптимизация: Удалять только необходимые записи
            List<Handbook> existingHandbooks = handbookRepository.findAll();
            List<Handbook> handbooksToDelete = existingHandbooks.stream()
                    .filter(h -> !collection.contains(h))
                    .collect(Collectors.toList());
            handbookRepository.deleteAll(handbooksToDelete);

            log.info("Deleted {} existing handbooks from the table", handbooksToDelete.size());

            // Оптимизация: Сохранение только новых или измененных записей
            List<Handbook> handbooksToSave = collection.stream()
                    .filter(h -> !existingHandbooks.contains(h) || !h.equals(existingHandbooks.get(existingHandbooks.indexOf(h))))
                    .collect(Collectors.toList());

            if (!handbooksToSave.isEmpty()) {
                handbookRepository.saveAll(handbooksToSave);
                log.info("Saved {} new or updated handbooks to the table", handbooksToSave.size());
            } else {
                log.info("No new or updated handbooks to save");
            }

            return "Handbook updated"; // or handle return value based on saveAll outcome
        } catch (Exception e) {
            log.error("Error saving handbooks", e);
            throw new RuntimeException("Failed to save handbooks", e); // Wrap in a custom exception if needed
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
        handbook.setRetailNetworkCode(getStringValue(str, 0));
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

    public List<String> getRetailNetwork() {
        return handbookRepository.findDistinctByRetailNetwork();
    }

}
