package ftn.iis.model;
import ftn.iis.enums.StatusPredloga;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "predlog_za_nabavku")
public class PredlogZaNabavku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "korisnik_jmbg", nullable = false)
    private User korisnik;

    @Column(name = "naslov", nullable = false)
    private String naslov;

    @Column(name = "autor", nullable = false)
    private String autor;

    @Column(name = "datum_podnosenja", nullable = false)
    private LocalDate datumPodnosenja;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPredloga status;

    @Column(name = "obrazlozenje")
    private String obrazlozenje;                 // popunjava bibliotekar samo pri odbijanju

    public PredlogZaNabavku() {}

    public PredlogZaNabavku(User korisnik, String naslov, String autor) {
        this.korisnik = korisnik;
        this.naslov = naslov;
        this.autor = autor;
        this.datumPodnosenja = LocalDate.now();
        this.status = StatusPredloga.NA_CEKANJU;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(User korisnik) {
        this.korisnik = korisnik;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getDatumPodnosenja() {
        return datumPodnosenja;
    }

    public void setDatumPodnosenja(LocalDate datumPodnosenja) {
        this.datumPodnosenja = datumPodnosenja;
    }

    public StatusPredloga getStatus() {
        return status;
    }

    public void setStatus(StatusPredloga status) {
        this.status = status;
    }

    public String getObrazlozenje() {
        return obrazlozenje;
    }

    public void setObrazlozenje(String obrazlozenje) {
        this.obrazlozenje = obrazlozenje;
    }

}