package ftn.iis.service;

import ftn.iis.dto.SistemskaPreporukaResponseDto;
import ftn.iis.enums.StatusSistemskePreporuke;
import ftn.iis.model.FizickaKnjiga;
import ftn.iis.model.SistemskaPreporuka;
import ftn.iis.repository.FizickaKnjigaRepository;
import ftn.iis.repository.PozajmicaRepository;
import ftn.iis.repository.RezervacijaRepository;
import ftn.iis.repository.SistemskePreporukeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SistemskePreporukeService {
    private final SistemskePreporukeRepository sistemskePreporukeRepository;
    private final FizickaKnjigaRepository fizickaKnjigaRepository;
    private final PozajmicaRepository pozajmicaRepository;

    private static final int BROJ_DANA_ANALIZE = 30;
    private static final int PRAG_POZAJMICA = 2;        // knjiga mora imati 3+ pozajmice

    public SistemskePreporukeService(SistemskePreporukeRepository sistemskePreporukeRepository, FizickaKnjigaRepository fizickaKnjigaRepository,
                                     PozajmicaRepository pozajmicaRepository) {
        this.sistemskePreporukeRepository = sistemskePreporukeRepository;
        this.fizickaKnjigaRepository = fizickaKnjigaRepository;
        this.pozajmicaRepository = pozajmicaRepository;
    }

    // rucno pokretanje analize trendova
    @Transactional
    public void generisiPreporuke () {
        LocalDate od = LocalDate.now().minusDays(BROJ_DANA_ANALIZE);

        List<FizickaKnjiga> sveFizickeKnjige = fizickaKnjigaRepository.findAll();

        for(FizickaKnjiga fk : sveFizickeKnjige) {
            String isbn = fk.getIsbn();

            // pribavi info o br pozajmica i rezervacija i primeraka
            Integer brPozajmica = pozajmicaRepository.countByIsbnAndDatPozAfter(isbn, od);
            Integer brPrimeraka = fizickaKnjigaRepository.countPrimerciByIsbn(isbn);

            if (brPozajmica >= PRAG_POZAJMICA) {
                boolean vecPostojiPreporuka = sistemskePreporukeRepository.existsByFizickaKnjigaIsbnAndStatus(isbn, StatusSistemskePreporuke.AKTIVNA);
                if (!vecPostojiPreporuka) {
                    String predlog = generisiTekstPreporuke(brPozajmica, brPrimeraka);

                    SistemskaPreporuka preporuka = new SistemskaPreporuka();
                    preporuka.setStatus(StatusSistemskePreporuke.AKTIVNA);
                    preporuka.setDatumGenerisanja(LocalDateTime.now());
                    preporuka.setFizickaKnjiga(fk);
                    preporuka.setBrojPozajmica(brPozajmica);
                    preporuka.setTrenutniBrojPrimeraka(brPrimeraka);
                    preporuka.setPredlog(predlog);
                    sistemskePreporukeRepository.save(preporuka);
                }
            }
        }
    }




    // automatsko pokretanje analize svakog ponedeljka u ponoc ~~~
    @Scheduled(cron = "0 0 0 * * MON")
    public void automatskaAnaliza() {
        generisiPreporuke();
    }



    // pomocne metodice ~~~

    private String generisiTekstPreporuke(int pozajmice, int primerci) {
        if (pozajmice > 10 && primerci < 3) {
            return "Visok broj rezervacija uz mali broj primeraka — preporučuje se hitna nabavka.";
        } else if (pozajmice >= PRAG_POZAJMICA * 2) {
            return "Knjiga beleži nagli porast pozajmica — preporučuje se nabavka dodatnih primeraka.";
        } else {
            return "Povećana potražnja — razmotrite nabavku dodatnih primeraka.";
        }
    }

    private SistemskaPreporukaResponseDto mapirajUDto(SistemskaPreporuka p) {
        SistemskaPreporukaResponseDto dto = new SistemskaPreporukaResponseDto();
        dto.setId(p.getId());
        dto.setIsbn(p.getFizickaKnjiga().getIsbn());
        dto.setNaslov(p.getFizickaKnjiga().getKnjiga().getNaslov());
        dto.setAutor(p.getFizickaKnjiga().getKnjiga().getAutor());
        dto.setBrojPozajmica(p.getBrojPozajmica());
        dto.setTrenutniBrojPrimeraka(p.getTrenutniBrojPrimeraka());
        dto.setPredlog(p.getPredlog());
        dto.setDatumGenerisanja(p.getDatumGenerisanja());
        dto.setStatus(p.getStatus());
        return dto;
    }

}
