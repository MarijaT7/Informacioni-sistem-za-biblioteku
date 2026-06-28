package ftn.iis.repository;

import ftn.iis.model.CetSesija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CetSesijaRepository extends JpaRepository<CetSesija, Long> {
    List<CetSesija> findByArhiviranoIsTrueAndDatumArhiviranjaCSBefore(LocalDateTime granica);

    // Pronalazi sve grane od date roditeljske sesije
    List<CetSesija> findByRoditeljskaSesija(CetSesija roditeljskaSesija);
}
