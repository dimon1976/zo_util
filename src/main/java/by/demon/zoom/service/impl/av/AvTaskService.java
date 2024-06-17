package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.imp.av.AvDataEntity;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.FileUtil.downloadFile;
import static by.demon.zoom.util.FileUtil.getPath;
import static by.demon.zoom.util.Globals.TEMP_PATH;

@Service
public class AvTaskService implements FileProcessingService<AvDataEntity> {

    private final AvTaskRepository avTaskRepository;
    private final DataToExcel<AvDataEntity> dataToExcel;
    private final static Logger log = LoggerFactory.getLogger(AvTaskService.class);
    private final List<String> header = Arrays.asList("Номер задания", "Старт задания", "Окончание задания", "Товар НО", "Категория", "Код товарной категории", "Описание товара", "Комментарий по товару", "Бренд", "Код ценовой зоны", "Код розничной сети", "Розничная сеть", "Регион", "Физический адрес", "Штрихкод",
            "Количество штук", "Цена конкурента", "Цена акционная/по карте", "Аналог", "Нет товара", "Дата мониторинга", "Фото", "Примечание", "Ссылка на страницу товара");

    public AvTaskService(AvTaskRepository avTaskRepository, DataToExcel<AvDataEntity> dataToExcel) {
        this.avTaskRepository = avTaskRepository;
        this.dataToExcel = dataToExcel;
    }

    public void download(ArrayList<AvDataEntity> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        String fileName = "av_task_data";
        Path path = getPath(fileName, format.equals("excel") ? ".xlsx" : ".csv");
        if (additionalParameters[1].equals("ЯНДЕКС_М_ОНЛ")) {
            path = Path.of(TEMP_PATH, "task_y.csv");
        }
        downloadFile(header, list, response, format, path, dataToExcel);
    }

    @Override
    public ArrayList<AvDataEntity> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<AvDataEntity> allTasks = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<AvDataEntity>>> futures = files.stream()
                .map(file -> executorService.<ArrayList<AvDataEntity>>submit(() -> {
                    try {
                        log.info("Processing file: {}", file.getName());
                        List<List<Object>> lists = readDataFromFile(file);
                        Files.delete(file.toPath());
                        return getTaskList(lists);
                    } catch (Exception e) {
                        log.error("Failed to process file: {}", file.getName(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                        return new ArrayList<>();
                    }
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<AvDataEntity>> future : futures) {
            try {
                allTasks.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }

        save(allTasks);
        return allTasks;
    }

    @Override
    public String save(ArrayList<AvDataEntity> taskArrayList) {
        try {
            avTaskRepository.saveAll(taskArrayList);
            log.info("Job file has been successfully saved");
            return "The job file has been successfully saved";
        } catch (Exception e) {
            log.error("Error saving tasks", e);
            throw new RuntimeException("Failed to save tasks", e);
        }
    }

    public ArrayList<AvDataEntity> getDto(String... additionalParameters) {
        ArrayList<AvDataEntity> byJobNumberAndRetailerCode = avTaskRepository.findByJobNumberAndRetailerCode(additionalParameters[0], additionalParameters[1] == null ? "" : additionalParameters[1]);
        for (AvDataEntity entity : byJobNumberAndRetailerCode) {
            entity.setNumberOfPieces("1");
        }
        return byJobNumberAndRetailerCode;
    }

    @Transactional
    public int deleteTask(String taskNum) {
        try {
            log.info("Deleting task with number: {}", taskNum);
            int deletedCount = avTaskRepository.deleteAllByField(taskNum);
            log.info("Deleted {} task(s) with number: {}", deletedCount, taskNum);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error deleting task with number: {}", taskNum, e);
            throw new RuntimeException("Error deleting task with number: " + taskNum, e);
        }
    }

    private ArrayList<AvDataEntity> getTaskList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createTaskFromList)
                .collect(Collectors.toCollection(ArrayList::new));
    }
//ref
    private AvDataEntity createTaskFromList(List<Object> str) {
        AvDataEntity task = new AvDataEntity();
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
        task.setRetailerCode(getStringValue(str, 10)); // Значение для сверки в отчете
        task.setRetailChain(getStringValue(str, 11));
        task.setRegion(getStringValue(str, 12));
        task.setPhysicalAddress(getStringValue(str, 13));
        task.setBarcode(getStringValue(str, 14));
        return task;
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }

    public LinkedHashSet<String> getLatestTask() {
        // Получаем последние 10 сохраненных заданий из базы данных нативным запросом
        return avTaskRepository.findDistinctTopByJobNumber();
    }
}
