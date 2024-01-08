package by.demon.zoom.dao;

import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvReportRepository extends JpaRepository<CsvAvReportEntity, Long> {

    @Query("select DISTINCT jobNumber FROM CsvAvReportEntity order by jobNumber desc ")
    List<String> findDistinctTopByJobNumber(Pageable pageable);

    List<CsvAvReportEntity> findAllByJobNumber(String task);

    @Modifying
    @Query("delete from CsvAvReportEntity where jobNumber = :report")
    void deleteAllByField(String report);
}
