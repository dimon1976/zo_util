package by.demon.zoom.dao;

import by.demon.zoom.domain.av.CsvDataEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvTaskRepository extends JpaRepository<CsvDataEntity, Long> {

    @Query("select a from CsvDataEntity a where a.retailerCode=:retailerCode and a.jobNumber=:jobNumber")
    List<CsvDataEntity> findByJobNumberAndRetailerCode(String jobNumber, String retailerCode);

    @Query("select DISTINCT jobNumber FROM CsvDataEntity order by jobNumber desc ")
    List<String> findDistinctTopByJobNumber(Pageable pageable);
}
