package by.demon.zoom.dao;

import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvReportRepository extends JpaRepository<CsvAvReportEntity, Long> {

    @Query(value = "SELECT DISTINCT job_number FROM av_report ORDER BY job_number DESC LIMIT 25", nativeQuery = true)
    List<String> findDistinctTopByJobNumber();

    List<CsvAvReportEntity> findAllByJobNumber(String task);

    @Modifying
    @Query("delete from CsvAvReportEntity where jobNumber = :report")
    void deleteAllByField(String report);
}
