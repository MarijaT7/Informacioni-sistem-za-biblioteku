package ftn.iis.service;

import ftn.iis.dto.PredlogNabavkaDto;
import ftn.iis.dto.PredlogNabavkaResponseDto;
import ftn.iis.enums.StatusPredloga;
import ftn.iis.enums.Uloge;
import ftn.iis.exception.NonBiblotekarViewingSuggestionsException;
import ftn.iis.exception.NonClanGivingSuggestions;
import ftn.iis.exception.NonClanViewingSuggestions;
import ftn.iis.exception.NonManagerViewingSuggestionsException;
import ftn.iis.model.PredlogZaNabavku;
import ftn.iis.model.User;
import ftn.iis.repository.NotifikacijaRepository;
import ftn.iis.repository.PredlogNabavkaRepository;
import ftn.iis.repository.UserRepository;
import ftn.iis.utils.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PredlogNabavkeService {

    private final PredlogNabavkaRepository predlogNabavkaRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final NotifikacijaRepository notifikacijaRepository;

    public PredlogNabavkeService(PredlogNabavkaRepository predlogNabavkaRepository, JwtService jwtService,
                                 UserRepository userRepository, NotifikacijaRepository notifikacijaRepository){
        this.predlogNabavkaRepository = predlogNabavkaRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.notifikacijaRepository = notifikacijaRepository;
    }

    @Transactional
    public PredlogNabavkaResponseDto kreirajPredlog(String token, PredlogNabavkaDto dto){
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new NonClanGivingSuggestions();
        }
        PredlogZaNabavku predlogZaNabavku = new PredlogZaNabavku(korisnik, dto.getNaslov(), dto.getAutor());
        predlogZaNabavku = predlogNabavkaRepository.save(predlogZaNabavku);
        return mapirajUDto(predlogZaNabavku);
    }

    @Transactional
    public List<PredlogNabavkaResponseDto> mojiPredlozi(String token){
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new NonClanViewingSuggestions();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository.findAllByKorisnikJmbgOrderByDatumPodnosenjaDesc(jmbg);

        List<PredlogNabavkaResponseDto> dtos = new ArrayList<>();
        for (PredlogZaNabavku predlog : predlozi) {
            dtos.add(mapirajUDto(predlog));
        }

        return dtos;
    }

    //menadžer vidi odobrene predloge
    @Transactional
    public List<PredlogNabavkaResponseDto> odobreniPredlozi(String token) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerViewingSuggestionsException();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository
                .findAllByStatusOrderByDatumPodnosenjaDesc(StatusPredloga.ODOBRENO);

        List<PredlogNabavkaResponseDto> dtos = new ArrayList<>();
        for (PredlogZaNabavku predlog : predlozi) {
            dtos.add(mapirajUDto(predlog));
        }

        return dtos;
    }

    //bibliotekar vidi predloge na čekanju
    @Transactional
    public List<PredlogNabavkaResponseDto> predloziNaCekanju(String token) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("BIBLIOTEKAR")) {
            throw new NonBiblotekarViewingSuggestionsException();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository
                .findAllByStatusOrderByDatumPodnosenjaDesc(StatusPredloga.NA_CEKANJU);

        List<PredlogNabavkaResponseDto> dtos = new ArrayList<>();
        for (PredlogZaNabavku predlog : predlozi) {
            dtos.add(mapirajUDto(predlog));
        }

        return dtos;
    }

    // Pomocna funkcijica
    private PredlogNabavkaResponseDto mapirajUDto(PredlogZaNabavku p) {
        PredlogNabavkaResponseDto dto = new PredlogNabavkaResponseDto();
        dto.setId(p.getId());
        dto.setNaslov(p.getNaslov());
        dto.setAutor(p.getAutor());
        dto.setDatumPodnosenja(p.getDatumPodnosenja());
        dto.setStatus(p.getStatus());
        dto.setObrazlozenje(p.getObrazlozenje());
        dto.setKorisnikIme(p.getKorisnik().getFirstName());
        dto.setKorisnikPrezime(p.getKorisnik().getLastName());
        return dto;
    }

}
