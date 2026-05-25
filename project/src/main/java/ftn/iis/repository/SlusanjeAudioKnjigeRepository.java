package ftn.iis.repository;

import ftn.iis.model.SlusanjeAudioKnjige;
import ftn.iis.model.id.SlusanjeAudioKnjigeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlusanjeAudioKnjigeRepository extends JpaRepository<SlusanjeAudioKnjige, SlusanjeAudioKnjigeId> {
    Optional<SlusanjeAudioKnjige> findTopByIdJmbgClanaAndIdIsbnAudioKnjigeOrderByDatumPoslednjegPristupaDesc(
            String jmbgClana,
            String isbnAudioKnjige
    );
}
