package ftn.iis.dto;

import ftn.iis.enums.TipCP;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;
import ftn.iis.model.OcenaCetPoruke;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CetPorukaDto {
    private Long id;
    private TipCP tipCP;
    private LocalDateTime datumKreiranjaCP;
    private String sadrzajCP;

    public CetPorukaDto(Long id, TipCP tipCP, LocalDateTime datumKreiranjaCP, String sadrzajCP) {
        this.id = id;
        this.tipCP = tipCP;
        this.datumKreiranjaCP = datumKreiranjaCP;
        this.sadrzajCP = sadrzajCP;
    }

    public static CetPorukaDto fromCetPoruka(CetPoruka cetPoruka) {
        return new CetPorukaDto(
                cetPoruka.getId(),
                cetPoruka.getTipCP(),
                cetPoruka.getDatumKreiranjaCP(),
                cetPoruka.getSadrzajCP()
        );
    }

    public CetPorukaDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipCP getTipCP() {
        return tipCP;
    }

    public void setTipCP(TipCP tipCP) {
        this.tipCP = tipCP;
    }

    public LocalDateTime getDatumKreiranjaCP() {
        return datumKreiranjaCP;
    }

    public void setDatumKreiranjaCP(LocalDateTime datumKreiranjaCP) {
        this.datumKreiranjaCP = datumKreiranjaCP;
    }

    public String getSadrzajCP() {
        return sadrzajCP;
    }

    public void setSadrzajCP(String sadrzajCP) {
        this.sadrzajCP = sadrzajCP;
    }
}
