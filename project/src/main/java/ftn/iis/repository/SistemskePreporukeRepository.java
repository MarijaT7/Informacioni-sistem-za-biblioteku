package ftn.iis.repository;

import ftn.iis.model.SistemskaPreporuka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SistemskePreporukeRepository extends JpaRepository<SistemskaPreporuka, Long> {

}
