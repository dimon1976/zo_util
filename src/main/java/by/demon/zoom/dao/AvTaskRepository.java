package by.demon.zoom.dao;

import by.demon.zoom.domain.av.AvDataEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvTaskRepository extends JpaRepository<AvDataEntity, Long> {

    @Query("select a from AvDataEntity a where a.retailerCode=:retailerCode and a.jobNumber=:jobNumber")
    List<AvDataEntity> findByJobNumberAndRetailerCode(String jobNumber, String retailerCode);

    @Query("select DISTINCT jobNumber FROM AvDataEntity order by jobNumber desc ")
    List<String> findDistinctTopByJobNumber(Pageable pageable);

    @Query("select DISTINCT retailerCode FROM AvDataEntity where jobNumber=:task")
    List<String> findDistinctByJobNumber(String task);



}
