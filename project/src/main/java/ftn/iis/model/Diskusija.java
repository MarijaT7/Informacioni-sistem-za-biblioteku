package ftn.iis.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diskusija")
public class Diskusija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_d")
    private Long id;

    @Column(name = "naslov_d", nullable = false)
    private String naslovD;

    @Column(name = "opis_d", columnDefinition = "TEXT")
    private String opisD;

    @Column(name = "datum_kreiranja_d", nullable = false)
    private LocalDateTime datumKreiranjaD;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "isbn", nullable = false)
    private Knjiga knjiga;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    @OneToMany(mappedBy = "diskusija", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Komentar> komentari = new ArrayList<>();

    public Diskusija() {
    }

    public Diskusija(Long id, String naslovD, String opisD, LocalDateTime datumKreiranjaD, Knjiga knjiga, User clan, List<Komentar> komentari) {
        this.id = id;
        this.naslovD = naslovD;
        this.opisD = opisD;
        this.datumKreiranjaD = datumKreiranjaD;
        this.knjiga = knjiga;
        this.clan = clan;
        this.komentari = komentari;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaslovD() {
        return naslovD;
    }

    public void setNaslovD(String naslovD) {
        this.naslovD = naslovD;
    }

    public String getOpisD() {
        return opisD;
    }

    public void setOpisD(String opisD) {
        this.opisD = opisD;
    }

    public LocalDateTime getDatumKreiranjaD() {
        return datumKreiranjaD;
    }

    public void setDatumKreiranjaD(LocalDateTime datumKreiranjaD) {
        this.datumKreiranjaD = datumKreiranjaD;
    }

    public Knjiga getKnjiga() {
        return knjiga;
    }

    public void setKnjiga(Knjiga knjiga) {
        this.knjiga = knjiga;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public List<Komentar> getKomentari() {
        return komentari;
    }

    public void setKomentari(List<Komentar> komentari) {
        this.komentari = komentari;
    }
}