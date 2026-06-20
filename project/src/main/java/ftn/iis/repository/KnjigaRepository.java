package ftn.iis.repository;

import ftn.iis.model.Knjiga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface KnjigaRepository extends JpaRepository<Knjiga, String> {
    Optional<Knjiga> findByIsbn(String isbn);
    List<Knjiga> findByDeletedFalse();
    @Query("SELECT k FROM Knjiga k LEFT JOIN FETCH k.fizickaKnjiga WHERE k.isbn = :isbn")
    Optional<Knjiga> findByIsbnWithFizicka(@Param("isbn") String isbn);
    List<Knjiga> findByNaslovContainingIgnoreCaseAndDeletedFalse(String naslov);
    @Query("SELECT k\n" +
            "FROM Knjiga k\n" +
            "WHERE k.zanr.id IN :zanrIds\n" +
            "AND k.deleted = false")
    List<Knjiga> findByZanroviIdInAndDeletedFalse(@Param("zanrIds") Set<Long> zanrIds);

    @Query("""
    SELECT DISTINCT k.autor
    FROM Knjiga k
    WHERE k.zanr.id IN :zanrIds
    AND k.autor IS NOT NULL
    AND k.deleted = false
    """)
    List<String> findAutoriByZanrIds(@Param("zanrIds") Set<Long> zanrIds);

}
