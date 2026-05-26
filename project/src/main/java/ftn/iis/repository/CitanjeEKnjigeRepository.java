package ftn.iis.repository;

import ftn.iis.model.CitanjeEKnjige;
import ftn.iis.model.id.CitanjeEKnjigeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitanjeEKnjigeRepository extends JpaRepository<CitanjeEKnjige, CitanjeEKnjigeId> {
    Optional<CitanjeEKnjige> findTopByIdJmbgClanaAndIdIsbnEKnjigeOrderByDatumPoslednjegPristupaDesc(
            String jmbgClana,
            String isbnEKnjige
    );
}
