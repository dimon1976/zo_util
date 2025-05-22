package by.demon.zoom.util;

import by.demon.zoom.dao.FileMetaDataRepository;
import by.demon.zoom.domain.FileMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static by.demon.zoom.util.Globals.TEMP_PATH;
import static org.apache.commons.io.FilenameUtils.getExtension;

@Service
public class FileUploadHandler {
    private static final Logger log = LoggerFactory.getLogger(FileUploadHandler.class);
    private final FileMetaDataRepository fileMetaDataRepository;

    public FileUploadHandler(FileMetaDataRepository fileMetaDataRepository) {
        this.fileMetaDataRepository = fileMetaDataRepository;
    }


    public List<File> getFiles(MultipartFile[] multipartFiles) {
        FileUploadHandler handler = new FileUploadHandler(fileMetaDataRepository);
        return Arrays.stream(multipartFiles)
                .filter(handler::ifExist)
                .map(handler::saveFileAndGetPath)
                .filter(Objects::nonNull)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    private Path saveFileAndGetPath(MultipartFile file) {
        try {
            Path filePath = getFilePath(file);
            File transferTo = new File(filePath.toAbsolutePath().toString());
            createTempDirectory();

            try (OutputStream os = new FileOutputStream(transferTo)) {
                os.write(file.getBytes());

                saveFileMetaData(file);

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

    public void saveFileMetaData(MultipartFile file) throws IOException {
        // Сохранение метаданных файла
        String fileName = file.getOriginalFilename();
        String fileType = getExtension(fileName);
        FileMetaData metadata = new FileMetaData();
        metadata.setFileName(fileName);
        metadata.setFileType(fileType);
        metadata.setUploadTime(java.time.LocalDateTime.now());
        metadata.setFileSize(file.getSize());
        metadata.setFileHash(computeHash(file.getBytes()));
        fileMetaDataRepository.save(metadata);
    }

    private String computeHash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Ошибка при вычислении хеша файла", e);
            return "";
        }
    }

    private void createTempDirectory() {
        File directory = new File(TEMP_PATH);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                log.info("Directory created successfully: {}", TEMP_PATH);
            } else {
                log.error("Failed to create directory: {}", TEMP_PATH);
            }
        }
    }

    private boolean ifExist(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private Path getFilePath(MultipartFile multipartFile) {
        String orgName = multipartFile.getOriginalFilename();
        assert orgName != null;
        String extension = getExtension(orgName);
        return Path.of(TEMP_PATH + "/" + orgName.replace("." + extension, "-" + "out." + extension));
    }
}
