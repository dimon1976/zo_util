package by.demon.zoom.dao;

import by.demon.zoom.domain.av.Handbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HandbookRepository extends JpaRepository<Handbook, Long> {

    @Query("select DISTINCT retailNetwork FROM Handbook order by retailNetwork desc ")
    List<String>findDistinctByRetailNetwork();

}
