package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.av.CsvDataEntity;
import by.demon.zoom.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;

@Service
public class AvTaskService implements FileProcessingService<CsvDataEntity> {

    private final AvTaskRepository avTaskRepository;
    private final static Logger log = LoggerFactory.getLogger(AvTaskService.class);

    public AvTaskService(AvTaskRepository avTaskRepository) {
        this.avTaskRepository = avTaskRepository;
    }


    @Override
    public ArrayList<CsvDataEntity> readFiles(List<File> files, String... additionalParams) throws IOException {
//        Long start = MethodPerformance.start();
//        ArrayList<CsvDataEntity> allTasks = new ArrayList<>();
//
//        for (File file : files) {
//            try {
//                log.info("Processing file: {}", file.getName());
//                List<List<Object>> lists = readDataFromFile(file);
//                ArrayList<CsvDataEntity> taskArrayList = getTaskList(lists);
//                allTasks.addAll(taskArrayList);
//                String jobNumber = taskArrayList.get(2).getJobNumber();
//                log.info("Task {} successfully read and processed", jobNumber);
//            } catch (IOException e) {
//                log.error("Error reading data from file: {}", file.getAbsolutePath(), e);
//            } catch (Exception e) {
//                log.error("Error processing file: {}", file.getAbsolutePath(), e);
//            } finally {
//                if (file.exists()) {
//                    if (!file.delete()) {
//                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
//                    } else {
//                        log.info("File {} successfully deleted", file.getName());
//                    }
//                }
//            }
//        }
//        MethodPerformance.finish(start, "Время обработки файлов заданий ленты");
//        try {
//            save(allTasks);
//            return allTasks;
//        } catch (Exception e) {
//            log.error("Error saving tasks", e);
//            throw new RuntimeException("Failed to save tasks", e);
//        }
        ArrayList<CsvDataEntity> allTasks = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<CsvDataEntity>>> futures = files.stream()
                .map(file -> executorService.submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        List<List<Object>> lists = readDataFromFile(file);
                        Files.delete(file.toPath());
                        return getTaskList(lists);
                    } catch (IOException e) {
                        log.error("Error reading data from file: {}", file.getAbsolutePath(), e);
                    } catch (Exception e) {
                        log.error("Error processing file: {}", file.getAbsolutePath(), e);
                    }
                    return null;
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<CsvDataEntity>> future : futures) {
            try {
                ArrayList<CsvDataEntity> taskArrayList = future.get();
                allTasks.addAll(taskArrayList);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
            }
        }
        executorService.shutdown();

        try {
            save(allTasks);
            return allTasks;
        } catch (Exception e) {
            log.error("Error saving tasks", e);
            throw new RuntimeException("Failed to save tasks", e);
        }
    }

    @Override
    public String save(ArrayList<CsvDataEntity> taskArrayList) {
        try {
            avTaskRepository.saveAll(taskArrayList);
            log.info("Job file has been successfully saved");
            return "The job file has been successfully saved";
        } catch (Exception e) {
            log.error("Error saving tasks", e);
            throw new RuntimeException("Failed to save tasks", e);
        }
    }


    private ArrayList<CsvDataEntity> getTaskList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createTaskFromList)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private CsvDataEntity createTaskFromList(List<Object> str) {
        CsvDataEntity task = new CsvDataEntity();
        task.setJobNumber(getStringValue(str, 0));
        task.setJobStart(getStringValue(str, 1));
        task.setJobEnd(getStringValue(str, 2));
        task.setItemNumber(getStringValue(str, 3));
        task.setCategory(getStringValue(str, 4));
        task.setProductCategoryCode(getStringValue(str, 5));
        task.setProductDescription(getStringValue(str, 6));
        task.setProductComment(getStringValue(str, 7));
        task.setBrand(getStringValue(str, 8));
        task.setPriceZoneCode(getStringValue(str, 9));
        task.setRetailerCode(getStringValue(str, 10));
        task.setRetailChain(getStringValue(str, 11));
        task.setRegion(getStringValue(str, 12));
        task.setPhysicalAddress(getStringValue(str, 13));
        task.setBarcode(getStringValue(str, 14));
        return task;
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }

    public List<String> getLatestTask() {
        // Получаем последние 10 сохраненных заданий из базы данных
        Pageable pageable = PageRequest.of(0, 10);
        return avTaskRepository.findDistinctTopByJobNumber(pageable);
    }
}
