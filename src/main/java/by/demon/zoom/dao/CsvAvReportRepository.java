package by.demon.zoom.dao;

import by.demon.zoom.domain.imp.av.CsvAvReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsvAvReportRepository extends JpaRepository<CsvAvReportEntity, Long> {
}
