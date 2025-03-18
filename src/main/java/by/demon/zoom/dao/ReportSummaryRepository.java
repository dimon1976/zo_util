package by.demon.zoom.dao;

import by.demon.zoom.domain.imp.av.ReportSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ReportSummaryRepository extends JpaRepository<ReportSummary, Long> {

    @Query("SELECT r FROM ReportSummary r " +
            "WHERE (:city IS NULL OR r.city = :city) " +
            "AND (:typeReport IS NULL OR r.typeReport = :typeReport) " +
            "ORDER BY r.uploadTime ASC")
    List<ReportSummary> findByCityAndTypeReport(@Param("city") String city, @Param("typeReport") String typeReport);

    @Query("SELECT DISTINCT r.retailChain FROM ReportSummary r")
    List<String> findDistinctRetailChains();


    @Transactional
    @Modifying
    @Query("delete from ReportSummary where task_no = :task")
    int deleteAllByTask_no(String task);
}
