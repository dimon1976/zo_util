package by.demon.zoom.dao;

import by.demon.zoom.domain.av.CsvReportEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvReportRepository extends JpaRepository<CsvReportEntity, Long> {

    @Query("select DISTINCT jobNumber FROM CsvReportEntity order by jobNumber desc ")
    List<String> findDistinctTopByJobNumber(Pageable pageable);

    List<CsvReportEntity> findAllByJobNumber(String task);

}
