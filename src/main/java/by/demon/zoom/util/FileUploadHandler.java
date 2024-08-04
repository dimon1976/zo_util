package by.demon.zoom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static by.demon.zoom.util.Globals.TEMP_PATH;

public class FileUploadHandler {
    private static final Logger log = LoggerFactory.getLogger(FileUploadHandler.class);


    public static List<File> getFiles(MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .filter(FileUploadHandler::ifExist)
                .map(FileUploadHandler::saveFileAndGetPath)
                .filter(Objects::nonNull)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    private static Path saveFileAndGetPath(MultipartFile file) {
        try {
            Path filePath = getFilePath(file);
            File transferTo = new File(filePath.toAbsolutePath().toString());
            createTempDirectory();

            try (OutputStream os = new FileOutputStream(transferTo)) {
                os.write(file.getBytes());
                log.info("File uploaded successfully: {}", file.getOriginalFilename());
                return filePath;
            } catch (IOException e) {
                log.error("Error saving file: {}", e.getMessage());
                throw new RuntimeException("Error saving file", e);
            }
        } catch (Exception e) {
            log.error("Error creating file path: {}", e.getMessage());
            throw new RuntimeException("Error creating file path", e);
        }
    }

    private static void createTempDirectory() {
        File directory = new File(TEMP_PATH);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                log.info("Directory created successfully: {}", TEMP_PATH);
            } else {
                log.error("Failed to create directory: {}", TEMP_PATH);
            }
        }
    }

    private static boolean ifExist(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private static Path getFilePath(MultipartFile multipartFile) {
        String orgName = multipartFile.getOriginalFilename();
        assert orgName != null;
        String extension = getExtension(orgName);
        return Path.of(TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension));
    }

    private static String getExtension(String orgName) {
        return orgName.lastIndexOf(".") == -1 ? "" : orgName.substring(orgName.lastIndexOf(".") + 1);
    }
}
