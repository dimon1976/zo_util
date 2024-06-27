package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.imp.av.AvDataEntity;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
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
    private final ApplicationContext applicationContext;
    private final JdbcTemplate jdbcTemplate;
    private final AvTaskRepository avTaskRepository;
    private final DataToExcel<AvDataEntity> dataToExcel;
    private final static Logger LOGGER = LoggerFactory.getLogger(AvTaskService.class);
    private final List<String> header = Arrays.asList(
            "Номер задания", "Старт задания", "Окончание задания", "Товар НО", "Категория",
            "Код товарной категории", "Описание товара", "Комментарий по товару", "Бренд",
            "Код ценовой зоны", "Код розничной сети", "Розничная сеть", "Регион",
            "Физический адрес", "Штрихкод", "Количество штук", "Цена конкурента",
            "Цена акционная/по карте", "Аналог", "Нет товара", "Дата мониторинга",
            "Фото", "Примечание", "Ссылка на страницу товара"
    );

    public AvTaskService(ApplicationContext applicationContext, JdbcTemplate jdbcTemplate, AvTaskRepository avTaskRepository, DataToExcel<AvDataEntity> dataToExcel) {
        this.applicationContext = applicationContext;
        this.jdbcTemplate = jdbcTemplate;
        this.avTaskRepository = avTaskRepository;
        this.dataToExcel = dataToExcel;
    }
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        AvTaskService proxy = applicationContext.getBean(AvTaskService.class);
        proxy.createTempTableIfNotExists();
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
                .map(file -> executorService.submit(() -> processFile(file, errorMessages)))
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

    private ArrayList<AvDataEntity> processFile(File file, List<String> errorMessages) {
        try {
            List<List<Object>> lists = readDataFromFile(file);
            Files.delete(file.toPath());
            return getTaskList(lists);
        } catch (Exception e) {
            LOGGER.error("Failed to process file: {}", file.getName(), e);
            errorMessages.add("Failed to process file: " + file.getName() + " - " + e.getMessage());
            return new ArrayList<>();
        }
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
        ArrayList<AvDataEntity> tasks = avTaskRepository.findByJobNumberAndRetailerCode(additionalParameters[0], Optional.ofNullable(additionalParameters[1]).orElse(""));
        tasks.forEach(task -> task.setNumberOfPieces("1"));
        return tasks;
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
                .filter(row -> !"Номер задания".equals(row.get(0)))
                .map(this::createTaskFromList)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private AvDataEntity createTaskFromList(List<Object> row) {
        AvDataEntity task = new AvDataEntity();
        task.setJobNumber(getStringValue(row, 0));
        task.setJobStart(getStringValue(row, 1));
        task.setJobEnd(getStringValue(row, 2));
        task.setItemNumber(getStringValue(row, 3));
        task.setCategory(getStringValue(row, 4));
        task.setProductCategoryCode(getStringValue(row, 5));
        task.setProductDescription(getStringValue(row, 6));
        task.setProductComment(getStringValue(row, 7));
        task.setBrand(getStringValue(row, 8));
        task.setPriceZoneCode(getStringValue(row, 9));
        task.setRetailerCode(getStringValue(row, 10));
        task.setRetailChain(getStringValue(row, 11));
        task.setRegion(getStringValue(row, 12));
        task.setPhysicalAddress(getStringValue(row, 13));
        task.setBarcode(getStringValue(row, 14));
        return task;
    }

    private String getStringValue(List<Object> list, int index) {
        return (index >= 0 && index < list.size()) ? String.valueOf(list.get(index)) : "";
    }

    @Transactional
    public LinkedHashSet<String> getLatestTask() {
        String selectQuery = "SELECT job_number FROM tmp_av_task_job_number ORDER BY job_number DESC LIMIT 25";
        List<String> jobNumbers = jdbcTemplate.queryForList(selectQuery, String.class);
        return new LinkedHashSet<>(jobNumbers);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateTempTable() {
        LOGGER.info("Scheduled task updateTempTable started");
        updateTempTableData();
        LOGGER.info("Scheduled task updateTempTable finished");
    }

    @Transactional
    public void updateTempTableData() {
        try {
            String truncateTableQuery = "TRUNCATE TABLE tmp_av_task_job_number";
            String insertQuery = "INSERT INTO tmp_av_task_job_number (job_number) SELECT DISTINCT job_number FROM av_task";

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
    @Transactional
    public void truncateTempTableData() {
        try {
            // Очистка временной таблицы
            String truncateTableQuery = "TRUNCATE TABLE tmp_av_task_job_number";
            LOGGER.info("Executing TRUNCATE TABLE query");
            jdbcTemplate.execute(truncateTableQuery);

            // Создание индекса
            String createIndexQuery = "CREATE INDEX IF NOT EXISTS idx_tmp_av_task_job_number ON tmp_av_task_job_number (job_number)";
            LOGGER.info("Executing CREATE INDEX query");
            jdbcTemplate.execute(createIndexQuery);

            LOGGER.info("Temp table truncated and index updated successfully");
        } catch (Exception e) {
            LOGGER.error("Error truncating temp table or updating index: {}", e.getMessage(), e);
        }
    }

    public void updateTempTableData(ArrayList<AvDataEntity> allTasks) {
        String insertQuery = "INSERT INTO tmp_av_task_job_number (job_number) VALUES (?)";

        try {
            Set<String> uniqueJobNumbers = allTasks.stream()
                    .map(AvDataEntity::getJobNumber)
                    .collect(Collectors.toSet());

            for (String jobNumber : uniqueJobNumbers) {
                try {
                    jdbcTemplate.update(insertQuery, jobNumber);
                    LOGGER.info("Job number {} inserted into tmp_av_task_job_number successfully.", jobNumber);
                } catch (Exception e) {
                    LOGGER.error("Error inserting job number {}: ", jobNumber, e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error updating tmp_av_task_job_number: ", e);
        }
    }
    @Transactional
    public void createTempTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS tmp_av_task_job_number (" +
                "job_number VARCHAR(255) PRIMARY KEY" +
                ")";
        String createIndexQuery = "CREATE INDEX IF NOT EXISTS idx_tmp_av_task_job_number ON tmp_av_task_job_number (job_number)";

        try {
            jdbcTemplate.execute(createTableQuery);
            jdbcTemplate.execute(createIndexQuery);
            LOGGER.info("Temporary table tmp_av_task_job_number created successfully if it did not exist.");
        } catch (Exception e) {
            LOGGER.error("Error creating temporary table tmp_av_task_job_number: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create temporary table tmp_av_task_job_number", e);
        }
    }


}
