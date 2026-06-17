package ftn.iis.model;

import ftn.iis.enums.TipAgentaCS;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cet_sesija")
public class CetSesija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cs")
    private Long id;

    @Column(name = "naslov_cs")
    private String naslovCS;

    @Column(name = "datum_kreiranja_cs", nullable = false)
    private LocalDateTime datumKreiranjaCS;

    @Column(name = "datum_azuriranja_cs", nullable = false)
    private LocalDateTime datumAzuriranjaCS;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_agenta_cs", nullable = false)
    private TipAgentaCS tipAgentaCS;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    @OneToMany(mappedBy = "cetSesija", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CetPoruka> poruke = new ArrayList<>();

    public CetSesija() {
    }

    public CetSesija(Long id, String naslovCS, LocalDateTime datumKreiranjaCS, LocalDateTime datumAzuriranjaCS, TipAgentaCS tipAgentaCS, User clan, List<CetPoruka> poruke) {
        this.id = id;
        this.naslovCS = naslovCS;
        this.datumKreiranjaCS = datumKreiranjaCS;
        this.datumAzuriranjaCS = datumAzuriranjaCS;
        this.tipAgentaCS = tipAgentaCS;
        this.clan = clan;
        this.poruke = poruke;
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

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public List<CetPoruka> getPoruke() {
        return poruke;
    }

    public void setPoruke(List<CetPoruka> poruke) {
        this.poruke = poruke;
    }
}