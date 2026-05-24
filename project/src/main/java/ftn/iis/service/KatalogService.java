package ftn.iis.service;

import ftn.iis.dto.KatalogDto;
import ftn.iis.model.Katalog;
import ftn.iis.repository.KatalogRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class KatalogService {
    private final KatalogRepository katalogRepository;

    public KatalogService(KatalogRepository katalogRepository) {
        this.katalogRepository = katalogRepository;
    }

    public boolean addNewCatalog(Katalog katalog){
        try{
            katalogRepository.saveAndFlush(katalog);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public Optional<Katalog> getByKatId(Long katId){
        return katalogRepository.findByKatId(katId);
    }
}
