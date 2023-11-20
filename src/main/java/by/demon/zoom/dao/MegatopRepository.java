package by.demon.zoom.dao;

import by.demon.zoom.domain.Megatop;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MegatopRepository extends JpaRepository<Megatop, Long> {

    @Query("SELECT DISTINCT m.label FROM Megatop m ORDER BY m.label DESC")
    List<String> findTop10DistinctLabels(Pageable pageable);

    List<Megatop> findByLabel(String label);

}
