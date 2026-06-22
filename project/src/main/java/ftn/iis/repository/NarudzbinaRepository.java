package ftn.iis.repository;

import ftn.iis.model.Narudzbina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface NarudzbinaRepository extends JpaRepository<Narudzbina, Long> {
}
