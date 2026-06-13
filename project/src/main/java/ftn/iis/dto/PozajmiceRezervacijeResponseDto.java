package ftn.iis.dto;

import java.util.List;

public class PozajmiceRezervacijeResponseDto {
    private List<PozajmicaDto> aktivnePozajmice;
    private List<PozajmicaDto> istorijaPozajmica;
    private List<RezervacijaDto> aktivneRezervacije;

    public PozajmiceRezervacijeResponseDto() {}

    public List<PozajmicaDto> getAktivnePozajmice() { return aktivnePozajmice; }
    public void setAktivnePozajmice(List<PozajmicaDto> aktivnePozajmice) { this.aktivnePozajmice = aktivnePozajmice; }
    public List<PozajmicaDto> getIstorija() { return istorijaPozajmica; }
    public void setIstorija(List<PozajmicaDto> istorijaPozajmica) { this.istorijaPozajmica = istorijaPozajmica; }
    public List<RezervacijaDto> getAktivneRezervacije() { return aktivneRezervacije; }
    public void setAktivneRezervacije(List<RezervacijaDto> aktivneRezervacije) { this.aktivneRezervacije = aktivneRezervacije; }

}
