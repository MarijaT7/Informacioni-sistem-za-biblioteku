package ftn.iis.dto;

import ftn.iis.enums.TipAgentaCS;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CetSesijaDetaljnoDto {
    private Long id;
    private String naslovCS;
    private LocalDateTime datumKreiranjaCS;
    private LocalDateTime datumAzuriranjaCS;
    private TipAgentaCS tipAgentaCS;
    private String jmbgClana;
    private List<CetPorukaDto> poruke;

    public CetSesijaDetaljnoDto(Long id, String naslovCS, LocalDateTime datumKreiranjaCS, LocalDateTime datumAzuriranjaCS, TipAgentaCS tipAgentaCS, String jmbgClana, List<CetPorukaDto> poruke) {
        this.id = id;
        this.naslovCS = naslovCS;
        this.datumKreiranjaCS = datumKreiranjaCS;
        this.datumAzuriranjaCS = datumAzuriranjaCS;
        this.tipAgentaCS = tipAgentaCS;
        this.jmbgClana = jmbgClana;
        this.poruke = poruke;
    }

    public CetSesijaDetaljnoDto() {
    }

    public static CetSesijaDetaljnoDto fromCetSesija(CetSesija cetSesija) {
        return new CetSesijaDetaljnoDto(
                cetSesija.getId(),
                cetSesija.getNaslovCS(),
                cetSesija.getDatumKreiranjaCS(),
                cetSesija.getDatumAzuriranjaCS(),
                cetSesija.getTipAgentaCS(),
                cetSesija.getClan().getJmbg(),
                cetSesija.getPoruke().stream()
                        .sorted(Comparator.comparing(CetPoruka::getDatumKreiranjaCP))
                        .map(CetPorukaDto::fromCetPoruka)
                        .collect(Collectors.toList())
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaslovCS() {
        return naslovCS;
    }

    public void setNaslovCS(String naslovCS) {
        this.naslovCS = naslovCS;
    }

    public LocalDateTime getDatumKreiranjaCS() {
        return datumKreiranjaCS;
    }

    public void setDatumKreiranjaCS(LocalDateTime datumKreiranjaCS) {
        this.datumKreiranjaCS = datumKreiranjaCS;
    }

    public LocalDateTime getDatumAzuriranjaCS() {
        return datumAzuriranjaCS;
    }

    public void setDatumAzuriranjaCS(LocalDateTime datumAzuriranjaCS) {
        this.datumAzuriranjaCS = datumAzuriranjaCS;
    }

    public TipAgentaCS getTipAgentaCS() {
        return tipAgentaCS;
    }

    public void setTipAgentaCS(TipAgentaCS tipAgentaCS) {
        this.tipAgentaCS = tipAgentaCS;
    }

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public List<CetPorukaDto> getPoruke() {
        return poruke;
    }

    public void setPoruke(List<CetPorukaDto> poruke) {
        this.poruke = poruke;
    }
}
