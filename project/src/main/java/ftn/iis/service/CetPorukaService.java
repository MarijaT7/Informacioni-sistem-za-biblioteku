package ftn.iis.service;

import ftn.iis.client.VektorskiServisClient;
import ftn.iis.dto.CetPorukaDto;
import ftn.iis.dto.KnjigeOdgovorDto;
import ftn.iis.dto.NovaCetPorukaOdgovorDto;
import ftn.iis.dto.RecenzijeOdgovorDto;
import ftn.iis.enums.TipAgentaCS;
import ftn.iis.enums.TipCP;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;
import ftn.iis.repository.CetPorukaRepository;
import ftn.iis.repository.CetSesijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class CetPorukaService {
    private final CetPorukaRepository cetPorukaRepository;
    private final CetSesijaRepository cetSesijaRepository;
    private final VektorskiServisClient vektorskiServisClient;

    public CetPorukaService(CetPorukaRepository cetPorukaRepository, CetSesijaRepository cetSesijaRepository, VektorskiServisClient vektorskiServisClient) {
        this.cetPorukaRepository = cetPorukaRepository;
        this.cetSesijaRepository = cetSesijaRepository;
        this.vektorskiServisClient = vektorskiServisClient;
    }

    public CetPoruka sacuvajPorukuClana(CetSesija cetSesija, String sadrzajPoruke) {
        CetPoruka korisnikovaPoruka = new CetPoruka();
        korisnikovaPoruka.setCetSesija(cetSesija);
        korisnikovaPoruka.setTipCP(TipCP.CLAN);
        korisnikovaPoruka.setDatumKreiranjaCP(LocalDateTime.now());
        korisnikovaPoruka.setSadrzajCP(sadrzajPoruke);
        return cetPorukaRepository.saveAndFlush(korisnikovaPoruka);
    }

    public CetPoruka pozoviAgentaISacuvajOdgovor(CetSesija cetSesija, String sadrzajPoruke) {
        String sadrzajOdgovora = pozoviAgentaIFormatirajOdgovor(cetSesija.getTipAgentaCS(), sadrzajPoruke);

        CetPoruka porukaAgenta = new CetPoruka();
        porukaAgenta.setCetSesija(cetSesija);
        porukaAgenta.setTipCP(TipCP.AI_ASISTENT);
        porukaAgenta.setDatumKreiranjaCP(LocalDateTime.now());
        porukaAgenta.setSadrzajCP(sadrzajOdgovora);
        return cetPorukaRepository.saveAndFlush(porukaAgenta);
    }

    @Transactional(rollbackFor = Exception.class)
    public NovaCetPorukaOdgovorDto postNovaCetPoruka(String jmbg, Long idCetSesije, String sadrzajPoruke) {
        CetSesija cetSesija = cetSesijaRepository.findById(idCetSesije)
                .orElseThrow(() -> new NoSuchElementException("Cet sesija ne postoji: " + idCetSesije));

        if (!Objects.equals(cetSesija.getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Cet sesija ne postoji: " + idCetSesije);
        }

        // 1. Sacuvaj poruku clana
        CetPoruka korisnikovaPoruka = sacuvajPorukuClana(cetSesija, sadrzajPoruke);

        // 2. Pozovi agenta (isti tip koji je vec postavljen u sesiji) i sacuvaj odgovor.
        //    Ako poziv ne uspe, izuzetak se propagira i ceo metod se rollback-uje
        //    (rollbackFor = Exception.class) - korisnikova poruka iz koraka 1 se ne cuva.
        CetPoruka porukaAgenta = pozoviAgentaISacuvajOdgovor(cetSesija, sadrzajPoruke);

        // 3. Azuriraj datum azuriranja sesije
        cetSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        cetSesijaRepository.saveAndFlush(cetSesija);

        return new NovaCetPorukaOdgovorDto(
                CetPorukaDto.fromCetPoruka(korisnikovaPoruka),
                CetPorukaDto.fromCetPoruka(porukaAgenta)
        );
    }

    private String pozoviAgentaIFormatirajOdgovor(TipAgentaCS tipAgentaCS, String poruka) {
        switch (tipAgentaCS) {
            case AGENT_KNJIGE:
                KnjigeOdgovorDto knjigeOdgovor = vektorskiServisClient.pitajAgentaKnjige(poruka);
                return knjigeOdgovor.getResponse();
            case AGENT_RECENZIJE:
                RecenzijeOdgovorDto recenzijeOdgovor = vektorskiServisClient.pitajAgentaRecenzije(poruka);
                return recenzijeOdgovor.getResponse();
            default:
                throw new IllegalArgumentException("Nepoznat tip agenta: " + tipAgentaCS);
        }
    }
}
