package ftn.iis.repository;
import ftn.iis.model.PredlogZaNabavku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredlogNabavkaRepository extends JpaRepository<PredlogZaNabavku, Long> {
    List<PredlogZaNabavku> findAllByKorisnikJmbgOrderByDatumPodnosenjaDesc(String jmbg);
    List<PredlogZaNabavku> findAllByOrderByDatumPodnosenjaDesc();
}
