package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "knjiga")
public class Knjiga {
    @Id
    @Column(name = "isbn", length = 13)
    private String isbn;

    @Column(name = "putanja_naslovna")
    private String putanjaNaslovna;

    @Column(name = "naslov", nullable = false)
    private String naslov;

    @Column(name = "sinopsis")
    private String sinopsis;

    @OneToOne(mappedBy = "knjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FizickaKnjiga fizickaKnjiga;

    @OneToOne(mappedBy = "knjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EKnjiga eKnjiga;

    @OneToOne(mappedBy = "knjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AudioKnjiga audioKnjiga;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "katalog_id", nullable = false)
    @JsonBackReference("katalog-knjiga")
    private Katalog katalog;

    public Knjiga() {
    }

    public Knjiga(String isbn, String putanjaNaslovna, String naslov, String sinopsis) {
        this.isbn = isbn;
        this.putanjaNaslovna = putanjaNaslovna;
        this.naslov = naslov;
        this.sinopsis = sinopsis;
    }

    public Knjiga(String isbn, String putanjaNaslovna, String naslov, String sinopsis, FizickaKnjiga fizickaKnjiga, EKnjiga eKnjiga, AudioKnjiga audioKnjiga, Katalog katalog) {
        this.isbn = isbn;
        this.putanjaNaslovna = putanjaNaslovna;
        this.naslov = naslov;
        this.sinopsis = sinopsis;
        this.fizickaKnjiga = fizickaKnjiga;
        this.eKnjiga = eKnjiga;
        this.audioKnjiga = audioKnjiga;
        this.katalog = katalog;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPutanjaNaslovna() {
        return putanjaNaslovna;
    }

    public void setPutanjaNaslovna(String putanjaNaslovna) {
        this.putanjaNaslovna = putanjaNaslovna;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public FizickaKnjiga getFizickaKnjiga() {
        return fizickaKnjiga;
    }

    public void setFizickaKnjiga(FizickaKnjiga fizickaKnjiga) {
        this.fizickaKnjiga = fizickaKnjiga;
    }

    public EKnjiga geteKnjiga() {
        return eKnjiga;
    }

    public void seteKnjiga(EKnjiga eKnjiga) {
        this.eKnjiga = eKnjiga;
    }

    public AudioKnjiga getAudioKnjiga() {
        return audioKnjiga;
    }

    public Katalog getKatalog() {
        return katalog;
    }

    public void setKatalog(Katalog katalog) {
        this.katalog = katalog;
    }

    public void setAudioKnjiga(AudioKnjiga audioKnjiga) {
        this.audioKnjiga = audioKnjiga;
    }
}