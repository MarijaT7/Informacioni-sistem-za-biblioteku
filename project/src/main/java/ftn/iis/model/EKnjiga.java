package ftn.iis.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "e_knjiga")
public class EKnjiga {
    @Id
    private String isbn;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "isbn")
    private Knjiga knjiga;

    @Column(name = "format_ek", length = 20)
    private String formatEK;

    @Column(name = "datum_dodavanja_ek")
    private LocalDate datumDodavanjaEK;

    @Column(name = "broj_strana_ek")
    private Integer brojStranaEK;

    @Column(name = "putanja_ek")
    private String putanjaEK;

    public EKnjiga() {
    }

    public EKnjiga(Knjiga knjiga, String formatEK, LocalDate datumDodavanjaEK, Integer brojStranaEK, String putanjaEK) {
        this.knjiga = knjiga;
        this.formatEK = formatEK;
        this.datumDodavanjaEK = datumDodavanjaEK;
        this.brojStranaEK = brojStranaEK;
        this.putanjaEK = putanjaEK;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Knjiga getKnjiga() {
        return knjiga;
    }

    public void setKnjiga(Knjiga knjiga) {
        this.knjiga = knjiga;
    }

    public String getFormatEK() {
        return formatEK;
    }

    public void setFormatEK(String formatEK) {
        this.formatEK = formatEK;
    }

    public LocalDate getDatumDodavanjaEK() {
        return datumDodavanjaEK;
    }

    public void setDatumDodavanjaEK(LocalDate datumDodavanjaEK) {
        this.datumDodavanjaEK = datumDodavanjaEK;
    }

    public Integer getBrojStranaEK() {
        return brojStranaEK;
    }

    public void setBrojStranaEK(Integer brojStranaEK) {
        this.brojStranaEK = brojStranaEK;
    }

    public String getPutanjaEK() {
        return putanjaEK;
    }

    public void setPutanjaEK(String putanjaEK) {
        this.putanjaEK = putanjaEK;
    }
}