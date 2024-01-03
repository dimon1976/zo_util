package by.demon.zoom.dao;

import by.demon.zoom.domain.imp.av.AvDataEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;

public interface AvTaskRepository extends JpaRepository<AvDataEntity, Long> {

    @Query("select a from AvDataEntity a where a.retailerCode=:retailerCode and a.jobNumber=:jobNumber")
    ArrayList<AvDataEntity> findByJobNumberAndRetailerCode(String jobNumber, String retailerCode);

    @Query("select DISTINCT jobNumber FROM AvDataEntity order by jobNumber desc ")
    ArrayList<String> findDistinctTopByJobNumber(Pageable pageable);

    @Query("select DISTINCT retailerCode FROM AvDataEntity where jobNumber=:task")
    ArrayList<String> findDistinctByJobNumber(String task);



}
