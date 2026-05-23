package ftn.iis.service;

import ftn.iis.dto.DobavljacDto;
import ftn.iis.dto.OsnovniDobavljacDto;
import ftn.iis.exception.*;
import ftn.iis.model.Dobavljac;
import ftn.iis.repository.DobavljacRepository;
import ftn.iis.utils.JwtService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        // 2. Provera preko Email-a
        if (dobavljacRepository.existsByEmail(dto.getEmail())) {
            throw new SupplierEmailAlreadyExists();
        }

        // 3. Provera preko naziva
        if (dobavljacRepository.existsByNaziv(dto.getNaziv())) {
            throw new SupplierNameAlreadyExists();
        }

        // 4. Provera preko telefona
        if (dobavljacRepository.existsByTel(dto.getNaziv())) {
            throw new SupplierPhoneAlreadyExists();
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

    public List<OsnovniDobavljacDto> ispisiSve(String token){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerViewingSupplierException();
        }

        // Ako je ulogovan menadzer, sledi ispis svih dobavljaca, osnovne info
        List<Dobavljac> detaljni = dobavljacRepository.findAll();
        List<OsnovniDobavljacDto> osnovni = new ArrayList<>();
        for (Dobavljac d : detaljni){
            osnovni.add( new OsnovniDobavljacDto(d.getNaziv(), d.getTel()));
        }
        return osnovni;
    }

    public Dobavljac ispisiJednog(String token, Long id){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerViewingSupplierException();
        }

        Dobavljac dobavljac = dobavljacRepository.findById(id).orElseThrow(() -> new NoSupplierFound());
        return dobavljac;
    }
}
