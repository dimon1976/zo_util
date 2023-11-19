package by.demon.zoom.dao;

import by.demon.zoom.domain.Megatop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MegatopCsvRBeanRepository extends JpaRepository<Megatop, Long> {

    List<Megatop> findDistinctTop10ByOrderByDateTimeDesc();

    List<Megatop> findByLabel(String label);

}
