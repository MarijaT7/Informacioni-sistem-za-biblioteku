package ftn.iis.repository;

import ftn.iis.model.Knjiga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnjigaRepository extends JpaRepository<Knjiga, String> {
    Optional<Knjiga> findByIsbn(String isbn);
    List<Knjiga> findByDeletedFalse();
    @Query("SELECT k FROM Knjiga k LEFT JOIN FETCH k.fizickaKnjiga WHERE k.isbn = :isbn")
    Optional<Knjiga> findByIsbnWithFizicka(@Param("isbn") String isbn);
    List<Knjiga> findByNaslovContainingIgnoreCaseAndDeletedFalse(String naslov);
}
