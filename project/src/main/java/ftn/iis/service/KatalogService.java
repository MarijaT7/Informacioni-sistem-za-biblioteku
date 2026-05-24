package ftn.iis.service;

import ftn.iis.model.Katalog;
import ftn.iis.repository.KatalogRepository;
import org.springframework.stereotype.Service;

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
}
