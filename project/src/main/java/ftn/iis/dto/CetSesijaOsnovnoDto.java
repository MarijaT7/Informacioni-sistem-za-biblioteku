package ftn.iis.dto;

import ftn.iis.enums.TipAgentaCS;
import ftn.iis.model.CetSesija;

import java.time.LocalDateTime;

public class CetSesijaOsnovnoDto {
    private Long id;
    private String naslovCS;
    private LocalDateTime datumKreiranjaCS;
    private LocalDateTime datumAzuriranjaCS;
    private TipAgentaCS tipAgentaCS;
    private String jmbgClana;

    public CetSesijaOsnovnoDto(Long id, String naslovCS, LocalDateTime datumKreiranjaCS, LocalDateTime datumAzuriranjaCS, TipAgentaCS tipAgentaCS, String jmbgClana) {
        this.id = id;
        this.naslovCS = naslovCS;
        this.datumKreiranjaCS = datumKreiranjaCS;
        this.datumAzuriranjaCS = datumAzuriranjaCS;
        this.tipAgentaCS = tipAgentaCS;
        this.jmbgClana = jmbgClana;
    }

    public CetSesijaOsnovnoDto() {
    }

    public static CetSesijaOsnovnoDto fromCetSesija(CetSesija cetSesija) {
        return new CetSesijaOsnovnoDto(
                cetSesija.getId(),
                cetSesija.getNaslovCS(),
                cetSesija.getDatumKreiranjaCS(),
                cetSesija.getDatumAzuriranjaCS(),
                cetSesija.getTipAgentaCS(),
                cetSesija.getClan().getJmbg()
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
}
