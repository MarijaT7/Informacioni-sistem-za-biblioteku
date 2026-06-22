package ftn.iis.repository;

import ftn.iis.model.Reklamacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReklamacijaRepository extends JpaRepository<Reklamacija, Long> {
}
