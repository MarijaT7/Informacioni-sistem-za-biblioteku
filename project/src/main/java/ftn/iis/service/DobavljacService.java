package ftn.iis.service;

import ftn.iis.dto.DobavljacDetaljniDto;
import ftn.iis.dto.DobavljacDto;
import ftn.iis.dto.OsnovniDobavljacDto;
import ftn.iis.exception.*;
import ftn.iis.model.Dobavljac;
import ftn.iis.model.Izdavac;
import ftn.iis.model.Knjizara;
import ftn.iis.repository.DobavljacRepository;
import ftn.iis.repository.IzdavacRepository;
import ftn.iis.repository.KnjizaraRepository;
import ftn.iis.utils.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DobavljacService {
    private final DobavljacRepository dobavljacRepository;
    private final JwtService jwtService;
    private final IzdavacRepository izdavacRepository;
    private final KnjizaraRepository knjizaraRepository;

    public DobavljacService(DobavljacRepository dobavljacRepository, JwtService jwtService,
                            IzdavacRepository izdavacRepository, KnjizaraRepository knjizaraRepository){
        this.dobavljacRepository = dobavljacRepository;
        this.jwtService = jwtService;
        this.izdavacRepository = izdavacRepository;
        this.knjizaraRepository = knjizaraRepository;
    }

    public DobavljacDetaljniDto kreirajDobavljaca(String token, DobavljacDto dto){
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
        if (dobavljacRepository.existsByNaziv(dto.getTel())) {
            throw new SupplierNameAlreadyExists();
        }

        // 4. Provera preko telefona
        if (dobavljacRepository.existsByTel(dto.getNaziv())) {
            throw new SupplierPhoneAlreadyExists();
        }

        // Ako je sve u redu, mapiram i čuvam OSNOVNOG dobavljaca
        Dobavljac dobavljac = new Dobavljac(
                dto.getNaziv(),
                dto.getEmail(),
                dto.getTel(),
                dto.getPib()
        );

        dobavljac = dobavljacRepository.save(dobavljac);

        String url = null;
        String tip = dto.getTipDobavljaca();
        //"00" → nije ni knjižara ni izdavač
        //"01" → samo knjižara
        //"10" → samo izdavač
        //"11" → i knjižara i izdavač

        // 2. Drugi karakter = knjižara
        if (tip.charAt(1) == '1') {
            Knjizara knjizara = new Knjizara(dobavljac);
            knjizara.setUrlOnlineProdavnice(dto.getUrlOnlineProdavnice());
            knjizaraRepository.save(knjizara);
            url = dto.getUrlOnlineProdavnice();
        }

        // 3. Prvi karakter = izdavač
        if (tip.charAt(0) == '1') {
            Izdavac izdavac = new Izdavac(dobavljac);
            izdavacRepository.save(izdavac);
        }

        // Isti DTO kao za detaljan prikaz
        DobavljacDetaljniDto rezultat = new DobavljacDetaljniDto();
        rezultat.setId(dobavljac.getId());
        rezultat.setNaziv(dobavljac.getNaziv());
        rezultat.setEmail(dobavljac.getEmail());
        rezultat.setTel(dobavljac.getTel());
        rezultat.setPib(dobavljac.getPib());
        rezultat.setTipDobavljaca(tip);
        rezultat.setUrlOnlineProdavnice(url);

        return rezultat;
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

    @Transactional
    public DobavljacDetaljniDto ispisiJednog(String token, Long id){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerViewingSupplierException();
        }

        Dobavljac dobavljac = dobavljacRepository.findById(id).orElseThrow(() -> new NoSupplierFound());

        //proveravam jel dobavljac i knjizara/izdavac kako bih ispisala dodatna polja po potrebi
        boolean jeIzdavac = dobavljac.getIzdavac() != null;
        boolean jeKnjizara = dobavljac.getKnjizara() != null;

        String tip = (jeIzdavac ? "1" : "0") + (jeKnjizara ? "1" : "0");
        String url = jeKnjizara ? dobavljac.getKnjizara().getUrlOnlineProdavnice() : null;

        DobavljacDetaljniDto dto = new DobavljacDetaljniDto();
        dto.setId(dobavljac.getId());
        dto.setNaziv(dobavljac.getNaziv());
        dto.setEmail(dobavljac.getEmail());
        dto.setTel(dobavljac.getTel());
        dto.setPib(dobavljac.getPib());
        dto.setTipDobavljaca(tip);
        dto.setUrlOnlineProdavnice(url);

        return dto;
    }
}
