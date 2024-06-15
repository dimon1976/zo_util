package by.demon.zoom.dao;

import by.demon.zoom.domain.av.AvHandbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvHandbookRepository extends JpaRepository<AvHandbook, Long> {

    @Query("select DISTINCT retailNetworkCode FROM AvHandbook ")
    List<String>findDistinctByRetailNetworkCode();

}
