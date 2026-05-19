package ftn.iis.model;

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

    public Knjiga() {
    }

    public Knjiga(String isbn, String putanjaNaslovna, String naslov, String sinopsis) {
        this.isbn = isbn;
        this.putanjaNaslovna = putanjaNaslovna;
        this.naslov = naslov;
        this.sinopsis = sinopsis;
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

    public void setAudioKnjiga(AudioKnjiga audioKnjiga) {
        this.audioKnjiga = audioKnjiga;
    }
}