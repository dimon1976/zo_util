package by.demon.zoom.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class FileMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fileName;
    private String fileType;
    private LocalDateTime uploadTime; // Новое поле для времени загрузки
    private Long fileSize; // Новое поле для размера файла в байтах
    private String fileHash; // Добавлено поле для хэша файла
}
