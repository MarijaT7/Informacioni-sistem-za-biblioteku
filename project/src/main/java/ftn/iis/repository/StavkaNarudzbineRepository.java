package ftn.iis.repository;

import ftn.iis.model.StavkaNarudzbine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StavkaNarudzbineRepository extends JpaRepository<StavkaNarudzbine, Long> {
}
