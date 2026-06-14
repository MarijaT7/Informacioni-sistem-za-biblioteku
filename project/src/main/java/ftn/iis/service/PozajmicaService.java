package ftn.iis.service;

import ftn.iis.dto.ObavestenjeDto;
import ftn.iis.dto.PozajmicaDto;
import ftn.iis.dto.PozajmiceRezervacijeResponseDto;
import ftn.iis.dto.RezervacijaDto;
import ftn.iis.model.*;
import ftn.iis.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PozajmicaService {

    private final PozajmicaRepository pozajmicaRepository;
    private final PrimerakKnjigeRepository primerakKnjigeRepository;
    private final RezervacijaRepository rezervacijaRepository;
    private final ProduzenjePozajmiceRepository produzenjePozajmiceRepository;
    private final ObavestenjeRepository obavestenjeRepository;
    private final UserRepository userRepository;
    private final KnjigaRepository knjigaRepository;
    private final EKnjigaRepository eKnjigaRepository;
    private final AudioKnjigaRepository audioKnjigaRepository;
    private final ClanarinaRepository clanarinaRepository;

    public PozajmicaService(PozajmicaRepository pozajmicaRepository, PrimerakKnjigeRepository primerkaKnjigeRepository, RezervacijaRepository rezervacijaRepository, ProduzenjePozajmiceRepository produzenjePozajmiceRepository, ObavestenjeRepository obavestenjeRepository, UserRepository userRepository, KnjigaRepository knjigaRepository, EKnjigaRepository eKnjigaRepository, AudioKnjigaRepository audioKnjigaRepository, ClanarinaRepository clanarinaRepository) {
        this.pozajmicaRepository = pozajmicaRepository;
        this.primerakKnjigeRepository = primerkaKnjigeRepository;
        this.rezervacijaRepository = rezervacijaRepository;
        this.produzenjePozajmiceRepository = produzenjePozajmiceRepository;
        this.obavestenjeRepository = obavestenjeRepository;
        this.userRepository = userRepository;
        this.knjigaRepository = knjigaRepository;
        this.eKnjigaRepository = eKnjigaRepository;
        this.audioKnjigaRepository = audioKnjigaRepository;
        this.clanarinaRepository = clanarinaRepository;
    }

    //da li korisnik ima aktivne pozajmice ili pozajmice gde je prekoracio rok vracanja
    public boolean userHasActiveOrOverdueLoan(String jmbg){
        return  pozajmicaRepository.hasOverduePozajmica(jmbg, LocalDate.now());
    }

    //pozajmljivanje fizickih knjiga
    @Transactional
    public Map<String, Object> borrowPhysicalBook(String jmbg, String isbn){
        Map<String, Object> result = new HashMap<>();

        User user= userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Korisnik nije pronađen.");
            return result;
        }
        List<Pozajmica> activeLoans = pozajmicaRepository.findByClan_JmbgAndStatusPozTrue(jmbg);
        boolean hasOverdue = activeLoans.stream().anyMatch(p -> p.getDatOcVrac().isBefore(LocalDate.now()));
        if (hasOverdue) {
            result.put("success", false);
            result.put("message", "Knjiga ne može biti pozajmljena pošto imate pozajmicu sa prekoračenim rokom vraćanja. Molimo vas da najpre vratite zakasnele knjige.");
            return result;
        }

        List<PrimerakKnjige> available = primerakKnjigeRepository.findAvailablePrimerciByIsbn(isbn);
        if (available.isEmpty()) {
            result.put("success", false);
            result.put("noAvailable", true);
            result.put("message", "Nema dostupnog primerka. Možete napraviti rezervaciju.");
            return result;
        }

        PrimerakKnjige primerak=available.get(0);
        LocalDate danas= LocalDate.now();
        LocalDate datOcVrac= danas.plusDays(14);
        Pozajmica pozajmica = new Pozajmica();
        pozajmica.setDatPoz(danas);
        pozajmica.setDatOcVrac(datOcVrac);
        pozajmica.setStatusPoz(true);
        pozajmica.setPrimerakKnjige(primerak);
        pozajmica.setClan(user);
        pozajmicaRepository.save(pozajmica);

        result.put("success", true);
        result.put("datPoz", danas.toString());
        result.put("datOcVrac", datOcVrac.toString());
        result.put("message", "Knjiga je uspešno pozajmljena i biće dostupna za preuzimanje u vašoj odabranoj biblioteci od " + formatDate(danas) + ". Važenje pozajmice od " + formatDate(danas) + " do " + formatDate(datOcVrac) + ".");
        return result;
    }
    private String formatDate(LocalDate date) {
        return String.format("%02d. %02d. %d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    @Transactional
    public Map<String, Object> borrowDigital(String jmbg, String isbn, String type) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Korisnik nije pronađen.");
            return result;
        }

        // Check for active/overdue loans
        List<Pozajmica> activeLoans = pozajmicaRepository.findByClan_JmbgAndStatusPozTrue(jmbg);
        boolean hasOverdue = activeLoans.stream().anyMatch(p -> p.getDatOcVrac().isBefore(LocalDate.now()));
        if (hasOverdue) {
            result.put("success", false);
            result.put("message", "Knjiga ne može biti pozajmljena pošto imate pozajmicu sa prekoračenim rokom vraćanja. Molimo vas da najpre vratite zakasnele knjige.");
            return result;
        }

        result.put("success", true);
        result.put("message", "Možete pristupiti " + (type.equals("audio") ? "audio knjizi" : "e-knjizi") + ".");
        return result;
    }

    //pravljenje rezervacija
    @Transactional
    public  Map<String, Object> makeReservation(String jmbg, String isbn){
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Korisnik nije pronađen.");
            return result;
        }
        Knjiga knjiga = knjigaRepository.findByIsbnWithFizicka(isbn).orElse(null);
        if (knjiga == null || knjiga.getFizickaKnjiga() == null) {
            result.put("success", false);
            result.put("message", "Knjiga nije pronađena.");
            return result;
        }
        FizickaKnjiga fizickaKnjiga = knjiga.getFizickaKnjiga();

        if (rezervacijaRepository.hasActiveRezervacija(jmbg, isbn)) {
            result.put("success", false);
            result.put("message", "Već imate aktivnu rezervaciju za ovu knjigu.");
            return result;
        }
        Optional<LocalDate> firstReturn = rezervacijaRepository.findFirstActivePozajmicaReturnDate(isbn);
        LocalDate datIspR = firstReturn.orElse(LocalDate.now().plusDays(14));

        Rezervacija rezervacija = new Rezervacija();
        rezervacija.setDatR(LocalDate.now());
        rezervacija.setKanalR("ONLINE");
        rezervacija.setDatIspR(datIspR);
        rezervacija.setFizickaKnjiga(fizickaKnjiga);
        rezervacija.setClan(user);
        rezervacijaRepository.save(rezervacija);

        result.put("success", true);
        result.put("datIspR", datIspR.toString());
        result.put("message", "Knjiga je uspešno rezervisana i očekuje se da će biti dostupna za preuzimanje u vašoj odabranoj biblioteci od " + formatDate(datIspR) + ".");
        return result;
    }
    @Transactional
    public PozajmiceRezervacijeResponseDto getPozajmiceAndRezervacije(String jmbg) {
        PozajmiceRezervacijeResponseDto response = new PozajmiceRezervacijeResponseDto();

        List<Pozajmica> allPozajmice = pozajmicaRepository.findByClan_Jmbg(jmbg);
        List<PozajmicaDto> aktivne = allPozajmice.stream()
                .filter(p -> Boolean.TRUE.equals(p.getStatusPoz()))
                .map(PozajmicaDto::fromPozajmica)
                .collect(Collectors.toList());
        List<PozajmicaDto> istorija = allPozajmice.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getStatusPoz()))
                .map(PozajmicaDto::fromPozajmica)
                .collect(Collectors.toList());

        List<Rezervacija> rezervacije = rezervacijaRepository.findByClan_Jmbg(jmbg);
        List<RezervacijaDto> aktivneRez = rezervacije.stream()
                .filter(r -> r.getDatObavR() == null)
                .map(RezervacijaDto::fromRezervacija)
                .collect(Collectors.toList());

        response.setAktivnePozajmice(aktivne);
        response.setIstorija(istorija);
        response.setAktivneRezervacije(aktivneRez);
        return response;
    }

    //produzenje
    @Transactional
    public Map<String, Object> requestExtension(Long pozajmicaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();

        Pozajmica pozajmica = pozajmicaRepository.findById(pozajmicaId).orElse(null);
        if (pozajmica == null || !pozajmica.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Pozajmica nije pronađena.");
            return result;
        }


        boolean hasPending = pozajmica.getProduzenja().stream().anyMatch(pp -> pp.getStatusPP() == null);
        if (hasPending) {
            result.put("success", false);
            result.put("message", "Već postoji zahtev za produženje koji čeka odobrenje.");
            return result;
        }

        ProduzenjePozajmice pp = new ProduzenjePozajmice();
        pp.setDatKrePP(LocalDate.now());
        pp.setDatObrPP(LocalDate.now().plusDays(10));
        pp.setKanalPP("ONLINE");
        pp.setStariDatVrac(pozajmica.getDatOcVrac());
        pp.setPozajmica(pozajmica);
        produzenjePozajmiceRepository.save(pp);

        result.put("success", true);
        result.put("message", "Zahtev za produženje je uspešno kreiran i čeka odobrenje bibliotekara.");
        return result;
    }

    //samo za bibliotekara
    @Transactional
    public Map<String, Object> processExtension(Long idPP, String bibliotekarJmbg, boolean approve, String razlog) {
        Map<String, Object> result = new HashMap<>();

        ProduzenjePozajmice pp = produzenjePozajmiceRepository.findById(idPP).orElse(null);
        if (pp == null) {
            result.put("success", false);
            result.put("message", "Zahtev nije pronađen.");
            return result;
        }

        User bibliotekar = userRepository.findByJmbg(bibliotekarJmbg).orElse(null);

        if (approve) {
            LocalDate noviDatVrac = pp.getPozajmica().getDatOcVrac().plusDays(14);
            pp.setStatusPP(true);
            pp.setNoviDatVrac(noviDatVrac);
            pp.setBibliotekar(bibliotekar);
            produzenjePozajmiceRepository.save(pp);

            // Update pozajmica return date
            Pozajmica pozajmica = pp.getPozajmica();
            pozajmica.setDatOcVrac(noviDatVrac);
            pozajmicaRepository.save(pozajmica);

            result.put("success", true);
            result.put("noviDatVrac", noviDatVrac.toString());
        } else {
            pp.setStatusPP(false);
            pp.setRazlogOdb(razlog);
            pp.setBibliotekar(bibliotekar);
            produzenjePozajmiceRepository.save(pp);
            result.put("success", true);
        }
        return result;
    }

    //izgubljena knjiga
    @Transactional
    public Map<String, Object> reportLostBook(Long pozajmicaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();
        Pozajmica pozajmica = pozajmicaRepository.findById(pozajmicaId).orElse(null);
        if (pozajmica == null || !pozajmica.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Pozajmica nije pronađena.");
            return result;
        }
        // Mark as returned (lost) - set returned date
        pozajmica.setStatusPoz(false);
        pozajmica.setDatVrac(LocalDate.now());
        pozajmicaRepository.save(pozajmica);

        result.put("success", true);
        result.put("message", "Prijava izgubljene knjige je evidentirana. Biće vam naplaćena naknada.");
        return result;
    }
    //vracanje knjige
    @Transactional
    public Map<String, Object> returnBook(Long pozajmicaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();
        Pozajmica pozajmica = pozajmicaRepository.findById(pozajmicaId).orElse(null);
        if (pozajmica == null || !pozajmica.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Pozajmica nije pronađena.");
            return result;
        }
        pozajmica.setStatusPoz(false);
        pozajmica.setDatVrac(LocalDate.now());
        pozajmicaRepository.save(pozajmica);

        checkAndNotifyReservation(pozajmica.getPrimerakKnjige().getFizickaKnjiga().getIsbn());

        result.put("success", true);
        result.put("message", "Knjiga je uspešno vraćena.");
        return result;
    }
    private void checkAndNotifyReservation(String isbn) {
        List<Rezervacija> activeRez = rezervacijaRepository.findActiveRezervacijeByIsbn(isbn);
        if (!activeRez.isEmpty()) {
            Rezervacija first = activeRez.get(0);
            first.setDatObavR(LocalDate.now());
            rezervacijaRepository.save(first);

            Obavestenje o = new Obavestenje();
            o.setTipO("REZERVACIJA_DOSTUPNA");
            o.setTekstO("Knjiga '" + first.getFizickaKnjiga().getKnjiga().getNaslov() + "' je dostupna za preuzimanje.");
            o.setDatKreiran(LocalDate.now());
            o.setClan(first.getClan());
            obavestenjeRepository.save(o);
        }
    }
    @Transactional
    public Map<String, Object> borrowFromReservation(Long rezervacijaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();

        Rezervacija rezervacija = rezervacijaRepository.findById(rezervacijaId).orElse(null);
        if (rezervacija == null || !rezervacija.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Rezervacija nije pronađena.");
            return result;
        }

        // Check for overdue loans
        if (pozajmicaRepository.hasOverduePozajmica(jmbg, LocalDate.now())) {
            result.put("success", false);
            result.put("message", "Knjiga ne može biti pozajmljena pošto još niste podmirili prethodna dugovanja");
            return result;
        }

        String isbn = rezervacija.getFizickaKnjiga().getIsbn();
        List<PrimerakKnjige> available = primerakKnjigeRepository.findAvailablePrimerciByIsbn(isbn);
        if (available.isEmpty()) {
            result.put("success", false);
            result.put("message", "Nema dostupnog primerka trenutno.");
            return result;
        }

        PrimerakKnjige primerak = available.get(0);
        LocalDate today = LocalDate.now();
        LocalDate datOcVrac = today.plusDays(14);

        Pozajmica pozajmica = new Pozajmica();
        pozajmica.setDatPoz(today);
        pozajmica.setDatOcVrac(datOcVrac);
        pozajmica.setStatusPoz(true);
        pozajmica.setPrimerakKnjige(primerak);
        pozajmica.setClan(rezervacija.getClan());
        pozajmica.setRezervacija(rezervacija);
        pozajmicaRepository.save(pozajmica);

        // Mark reservation as fulfilled
        rezervacija.setDatObavR(today);
        rezervacijaRepository.save(rezervacija);

        result.put("success", true);
        result.put("datPoz", today.toString());
        result.put("datOcVrac", datOcVrac.toString());
        result.put("message", "Knjiga je uspešno pozajmljena. Datum vraćanja: " + formatDate(datOcVrac));
        return result;
    }
    public List<ObavestenjeDto> getObavestenja(String jmbg) {
        return obavestenjeRepository.findByClan_JmbgOrderByDatKreiranDesc(jmbg)
                .stream()
                .map(ObavestenjeDto::fromObavestenje)
                .collect(Collectors.toList());
    }

    public void markObavestenjeRead(Long idO, String jmbg) {
        Obavestenje o = obavestenjeRepository.findById(idO).orElse(null);
        if (o != null && o.getClan().getJmbg().equals(jmbg)) {
            o.setProcitano(true);
            obavestenjeRepository.save(o);
        }
    }

    @Transactional
    public void deleteObavestenje(Long idO, String jmbg) {
        Obavestenje o = obavestenjeRepository.findById(idO).orElse(null);
        if (o != null && o.getClan().getJmbg().equals(jmbg)) {
            obavestenjeRepository.delete(o);
        }
    }
    public int getAvailableCopiesCount(String isbn) {
        return primerakKnjigeRepository.findAvailablePrimerciByIsbn(isbn).size();
    }

}
