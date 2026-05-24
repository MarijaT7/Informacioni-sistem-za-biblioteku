package ftn.iis.repository;

import ftn.iis.model.Knjiga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnjigaRepository extends JpaRepository<Knjiga, String> {
    Optional<Knjiga> findByIsbn(String isbn);
}
