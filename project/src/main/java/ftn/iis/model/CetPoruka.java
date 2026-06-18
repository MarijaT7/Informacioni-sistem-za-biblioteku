package ftn.iis.model;

import ftn.iis.enums.TipCP;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cet_poruka")
public class CetPoruka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cp")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_cp", nullable = false)
    private TipCP tipCP;

    @Column(name = "datum_kreiranja_cp", nullable = false)
    private LocalDateTime datumKreiranjaCP;

    @Column(name = "sadrzaj_cp", nullable = false, columnDefinition = "TEXT")
    private String sadrzajCP;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cs", nullable = false)
    private CetSesija cetSesija;

    @OneToMany(mappedBy = "cetPoruka", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OcenaCetPoruke> ocene = new ArrayList<>();

    public CetPoruka() {
    }

    public CetPoruka(Long id, TipCP tipCP, LocalDateTime datumKreiranjaCP, String sadrzajCP, CetSesija cetSesija, List<OcenaCetPoruke> ocene) {
        this.id = id;
        this.tipCP = tipCP;
        this.datumKreiranjaCP = datumKreiranjaCP;
        this.sadrzajCP = sadrzajCP;
        this.cetSesija = cetSesija;
        this.ocene = ocene;
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

    public CetSesija getCetSesija() {
        return cetSesija;
    }

    public void setCetSesija(CetSesija cetSesija) {
        this.cetSesija = cetSesija;
    }

    public List<OcenaCetPoruke> getOcene() {
        return ocene;
    }

    public void setOcene(List<OcenaCetPoruke> ocene) {
        this.ocene = ocene;
    }
}