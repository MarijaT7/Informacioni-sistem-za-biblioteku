package ftn.iis.service;

import ftn.iis.dto.DobavljacDto;
import ftn.iis.exception.NonManagerCreatingSupplierException;
import ftn.iis.exception.SupplierEmailAlreadyExists;
import ftn.iis.exception.SupplierPibAlreadyExists;
import ftn.iis.model.Dobavljac;
import ftn.iis.repository.DobavljacRepository;
import ftn.iis.utils.JwtService;
import org.springframework.stereotype.Service;

@Service
public class DobavljacService {
    private final DobavljacRepository dobavljacRepository;
    private final JwtService jwtService;

    public DobavljacService(DobavljacRepository dobavljacRepository, JwtService jwtService){
        this.dobavljacRepository = dobavljacRepository;
        this.jwtService = jwtService;
    }

    public Dobavljac kreirajDobavljaca(String token, DobavljacDto dto){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerCreatingSupplierException();
        }
        // Ako je ulogovan menadzer, sledi unos dobavljaca
        // 1. Provera preko PIB-a
        if (dobavljacRepository.existsByPib(dto.getPib())) {
            throw new SupplierPibAlreadyExists();
        }

        // 2. Provera preko Email-a (opciono, ako želiš i to da osiguraš)
        if (dobavljacRepository.existsByEmail(dto.getEmail())) {
            throw new SupplierEmailAlreadyExists();
        }

        // Ako je sve u redu, mapiram i čuvam dobavljaca
        Dobavljac dobavljac = new Dobavljac(
                dto.getNaziv(),
                dto.getEmail(),
                dto.getTel(),
                dto.getPib()
        );

        return dobavljacRepository.save(dobavljac);
    }
}
