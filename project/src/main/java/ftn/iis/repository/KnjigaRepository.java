package ftn.iis.repository;

import ftn.iis.model.Knjiga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnjigaRepository extends JpaRepository<Knjiga, String> {
    Optional<Knjiga> findByIsbn(String isbn);
    List<Knjiga> findByDeletedFalse();
    List<Knjiga> findByNaslovContainingIgnoreCaseAndDeletedFalse(String naslov);
}
