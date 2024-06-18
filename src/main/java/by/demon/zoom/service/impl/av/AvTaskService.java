package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.imp.av.AvDataEntity;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
    private final JdbcTemplate jdbcTemplate;
    private final AvTaskRepository avTaskRepository;
    private final DataToExcel<AvDataEntity> dataToExcel;
    private final static Logger LOGGER = LoggerFactory.getLogger(AvTaskService.class);
    private final List<String> header = Arrays.asList("Номер задания", "Старт задания", "Окончание задания", "Товар НО", "Категория", "Код товарной категории", "Описание товара", "Комментарий по товару", "Бренд", "Код ценовой зоны", "Код розничной сети", "Розничная сеть", "Регион", "Физический адрес", "Штрихкод",
            "Количество штук", "Цена конкурента", "Цена акционная/по карте", "Аналог", "Нет товара", "Дата мониторинга", "Фото", "Примечание", "Ссылка на страницу товара");

    public AvTaskService(JdbcTemplate jdbcTemplate, AvTaskRepository avTaskRepository, DataToExcel<AvDataEntity> dataToExcel) {
        this.jdbcTemplate = jdbcTemplate;
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
                        LOGGER.info("Processing file: {}", file.getName());
                        List<List<Object>> lists = readDataFromFile(file);
                        Files.delete(file.toPath());
                        return getTaskList(lists);
                    } catch (Exception e) {
                        LOGGER.error("Failed to process file: {}", file.getName(), e);
                        errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
                        return new ArrayList<>();
                    }
                }))
                .collect(Collectors.toList());

        for (Future<ArrayList<AvDataEntity>> future : futures) {
            try {
                allTasks.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error processing file", e);
                errorMessages.add("Error processing file: " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!errorMessages.isEmpty()) {
            throw new IOException("Some files failed to process: " + String.join(", ", errorMessages));
        }
        updateTempTableData(allTasks);
        save(allTasks);
        return allTasks;
    }

    @Override
    public String save(ArrayList<AvDataEntity> taskArrayList) {
        try {
            avTaskRepository.saveAll(taskArrayList);
            LOGGER.info("Job file has been successfully saved");
            return "The job file has been successfully saved";
        } catch (Exception e) {
            LOGGER.error("Error saving tasks", e);
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
            LOGGER.info("Deleting task with number: {}", taskNum);
            int deletedCount = avTaskRepository.deleteAllByField(taskNum);
            LOGGER.info("Deleted {} task(s) with number: {}", deletedCount, taskNum);
            deleteFromTempTable(taskNum);
            return deletedCount;
        } catch (Exception e) {
            LOGGER.error("Error deleting task with number: {}", taskNum, e);
            throw new RuntimeException("Error deleting task with number: " + taskNum, e);
        }
    }

    @Transactional
    public void deleteFromTempTable(String taskNum) {
        String deleteQuery = "DELETE FROM tmp_av_task_job_number WHERE job_number = ?";
        try {
            int rowsAffected = jdbcTemplate.update(deleteQuery, taskNum);
            LOGGER.info("Deleted {} rows from tmp_av_task_job_number where job_number = {}", rowsAffected, taskNum);
        } catch (Exception e) {
            LOGGER.error("Error deleting from tmp_av_task_job_number where job_number = {}: ", taskNum, e);
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

    //    public LinkedHashSet<String> getLatestTask() {
//        // Получаем последние 10 сохраненных заданий из базы данных нативным запросом
//        return avTaskRepository.findDistinctTopByJobNumber();
//    }
    @Transactional
    public LinkedHashSet<String> getLatestTask() {
        String selectQuery = "SELECT job_number FROM tmp_av_task_job_number ORDER BY job_number DESC LIMIT 25";
        List<String> jobNumbers = jdbcTemplate.queryForList(selectQuery, String.class);
        return new LinkedHashSet<>(jobNumbers);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Настроить cron выражение по необходимости
    public void updateTempTable() {
        updateTempTableData();
    }

    @Transactional
    public void updateTempTableData() {
        try {
            String truncateTableQuery = "TRUNCATE TABLE tmp_av_task_job_number";
            String insertQuery = "INSERT INTO tmp_av_task_job_number SELECT DISTINCT job_number FROM av_task";
            LOGGER.info("Executing TRUNCATE TABLE query");
            jdbcTemplate.execute(truncateTableQuery);
            LOGGER.info("Executing INSERT INTO query");
            jdbcTemplate.execute(insertQuery);
            LOGGER.info("Temp table data updated successfully");
        } catch (Exception e) {
            LOGGER.error("Error updating temp table data: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void updateTempTableIndex() {
        try {
            updateTempTable();
            String createIndexQuery = "CREATE INDEX IF NOT EXISTS idx_tmp_av_task_job_number ON tmp_av_task_job_number (job_number)";
            LOGGER.info("Executing CREATE INDEX query");
            jdbcTemplate.execute(createIndexQuery);
            LOGGER.info("Temp table index updated successfully");
        } catch (Exception e) {
            LOGGER.error("Error updating temp table index: {}", e.getMessage(), e);
        }
    }

    public void updateTempTableData(ArrayList<AvDataEntity> allTasks) {
        String truncateTableQuery = "TRUNCATE TABLE tmp_av_task_job_number";
        String insertQuery = "INSERT INTO tmp_av_task_job_number (job_number) VALUES (?)";

        try {
            // Truncate the table
            jdbcTemplate.execute(truncateTableQuery);
            LOGGER.info("Table tmp_av_task_job_number truncated successfully.");

            // Collect unique job numbers
            Set<String> uniqueJobNumbers = allTasks.stream()
                    .map(AvDataEntity::getJobNumber)
                    .collect(Collectors.toSet());

            // Insert each unique job number into the temp table
            uniqueJobNumbers.forEach(jobNumber -> {
                try {
                    jdbcTemplate.update(insertQuery, jobNumber);
                    LOGGER.info("Job number {} inserted into tmp_av_task_job_number successfully.", jobNumber);
                } catch (Exception e) {
                    LOGGER.error("Error inserting job number {}: ", jobNumber, e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error updating tmp_av_task_job_number: ", e);
        }
    }
}
