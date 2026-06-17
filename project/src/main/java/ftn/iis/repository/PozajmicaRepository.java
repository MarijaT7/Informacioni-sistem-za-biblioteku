package ftn.iis.repository;

import ftn.iis.model.Pozajmica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PozajmicaRepository extends JpaRepository<Pozajmica, Long> {
    List<Pozajmica> findByClan_JmbgAndStatusPozTrue(String jmbg);

    List<Pozajmica> findByClan_Jmbg(String jmbg);
    @Query("SELECT p FROM Pozajmica p WHERE p.clan.jmbg = :jmbg AND p.statusPoz = true AND p.datOcVrac < :today")
    List<Pozajmica> findOverduePozajmiceByJmbg(@Param("jmbg") String jmbg, @Param("today") LocalDate today);
    @Query("SELECT COUNT(p) > 0 FROM Pozajmica p WHERE p.clan.jmbg = :jmbg AND p.statusPoz = true AND p.datOcVrac < :today")
    boolean hasOverduePozajmica(@Param("jmbg") String jmbg, @Param("today") LocalDate today);
    @Query("SELECT p FROM Pozajmica p WHERE p.statusPoz = true AND p.datOcVrac = :targetDate")
    List<Pozajmica> findPozajmiceDueOn(@Param("targetDate") LocalDate targetDate);
    @Query("SELECT COUNT(p) FROM Pozajmica p WHERE p.primerakKnjige.fizickaKnjiga.isbn = :isbn AND p.datPoz >= :od")
    Integer countByIsbnAndDatPozAfter(@Param("isbn") String isbn, @Param("od") LocalDate od);

}
