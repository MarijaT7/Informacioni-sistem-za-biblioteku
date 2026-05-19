package ftn.iis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fizicka_knjiga")
public class FizickaKnjiga {
    @Id
    private String isbn;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "isbn")
    private Knjiga knjiga;

    public FizickaKnjiga() {
    }

    public FizickaKnjiga(Knjiga knjiga) {
        this.knjiga = knjiga;
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
}