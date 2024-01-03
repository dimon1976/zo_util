package by.demon.zoom.service.impl.av;

import by.demon.zoom.dao.AvTaskRepository;
import by.demon.zoom.domain.imp.av.AvDataEntity;
import by.demon.zoom.dto.CsvRow;
import by.demon.zoom.service.FileProcessingService;
import by.demon.zoom.util.DataDownload;
import by.demon.zoom.util.DataToExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static by.demon.zoom.util.FileDataReader.readDataFromFile;
import static by.demon.zoom.util.Globals.TEMP_PATH;

@Service
public class AvTaskService implements FileProcessingService<AvDataEntity> {

    private final AvTaskRepository avTaskRepository;
    private final DataDownload dataDownload;
    private final DataToExcel<AvDataEntity> dataToExcel;
    private final static Logger log = LoggerFactory.getLogger(AvTaskService.class);
    private final List<String> header = Arrays.asList("Номер задания", "Старт задания", "Окончание задания", "Товар НО", "Категория", "Код товарной категории", "Описание товара", "Комментарий по товару", "Бренд", "Код ценовой зоны", "Код розничной сети", "Розничная сеть", "Регион", "Физический адрес", "Штрихкод",
            "Количество штук", "Цена конкурента", "Цена акционная/по карте", "Аналог", "Нет товара", "Дата мониторинга", "Фото", "Примечание", "Ссылка на страницу товара");

    public AvTaskService(AvTaskRepository avTaskRepository, DataDownload dataDownload, DataToExcel<AvDataEntity> dataToExcel) {
        this.avTaskRepository = avTaskRepository;
        this.dataDownload = dataDownload;
        this.dataToExcel = dataToExcel;
    }

    public void download(ArrayList<AvDataEntity> list, HttpServletResponse response, String format, String... additionalParameters) throws IOException {
        Path path = DataDownload.getPath("data", format.equals("excel") ? ".xlsx" : ".csv");
        try {
            switch (format) {
                case "excel":
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        dataToExcel.exportToExcel(header, list, out, 0);
                        Files.write(path, out.toByteArray());
                    }
                    dataDownload.downloadExcel(path, response);
                    DataDownload.cleanupTempFile(path);
                    break;
                case "csv":
                    if (additionalParameters[1].equals("ЯНДЕКС_М_ОНЛ")){
                        path = Path.of(TEMP_PATH, "task_y.csv");
                    }
                    List<String> strings = convert(list);
                    dataDownload.downloadCsv(path, strings, header, response);
                    break;
                default:
                    log.error("Incorrect format: {}", format);
                    break;
            }

            log.info("Data exported successfully to {}: {}", format, path.getFileName().toString());
        } catch (IOException e) {
            log.error("Error exporting data to {}: {}", format, e.getMessage(), e);
            throw e;
        }
    }

    private List<String> convert(List<AvDataEntity> objectList) {
        return objectList.stream()
                .map(CsvRow::toCsvRow)
                .collect(Collectors.toList());
    }

    @Override
    public ArrayList<AvDataEntity> readFiles(List<File> files, String... additionalParams) throws IOException {
        ArrayList<AvDataEntity> allTasks = new ArrayList<>();

        int threadCount = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<ArrayList<AvDataEntity>>> futures = files.stream()
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

        for (Future<ArrayList<AvDataEntity>> future : futures) {
            try {
                ArrayList<AvDataEntity> taskArrayList = future.get();
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
        ArrayList<AvDataEntity> byJobNumberAndRetailerCode = avTaskRepository.findByJobNumberAndRetailerCode(additionalParameters[0], additionalParameters[1]);
        for (AvDataEntity entity : byJobNumberAndRetailerCode) {
            entity.setNumberOfPieces("1");
        }
        return byJobNumberAndRetailerCode;
    }


    private ArrayList<AvDataEntity> getTaskList(List<List<Object>> lists) {
        return lists.stream()
                .filter(str -> !"Номер задания".equals(str.get(0)))
                .map(this::createTaskFromList)
                .collect(Collectors.toCollection(ArrayList::new));
    }

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
        task.setRetailerCode(getStringValue(str, 10));// Значение для сверки в отчете
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
