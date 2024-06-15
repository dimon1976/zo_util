package by.demon.zoom.dao;

import by.demon.zoom.domain.imp.av.AvDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public interface AvTaskRepository extends JpaRepository<AvDataEntity, Long> {

    @Query("select a from AvDataEntity a where a.retailerCode=:retailerCode and a.jobNumber=:jobNumber")
    ArrayList<AvDataEntity> findByJobNumberAndRetailerCode(String jobNumber, String retailerCode);

    @Query(value = "SELECT DISTINCT job_number FROM av_task ORDER BY job_number DESC LIMIT 25", nativeQuery = true)
    LinkedHashSet<String> findDistinctTopByJobNumber();

    @Query("select DISTINCT retailerCode FROM AvDataEntity where jobNumber=:task")
    ArrayList<String> findDistinctByJobNumber(String task);

    @Modifying
    @Query("delete from AvDataEntity where jobNumber = :task")
    void deleteAllByField(String task);

}
