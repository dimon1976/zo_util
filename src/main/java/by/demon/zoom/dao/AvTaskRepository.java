package by.demon.zoom.dao;

import by.demon.zoom.domain.av.CsvDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvTaskRepository extends JpaRepository<CsvDataEntity, Long> {

}
