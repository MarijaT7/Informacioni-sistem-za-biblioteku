package ftn.iis.repository;
import ftn.iis.model.Notifikacija;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {
    List<Notifikacija> findAllByKorisnikJmbgOrderByDatumDesc(String jmbg);
    Integer countByKorisnikJmbgAndProcitanaFalse(String jmbg);
}