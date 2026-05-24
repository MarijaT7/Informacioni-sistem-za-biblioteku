package ftn.iis.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="katalog")
public class Katalog {

    @Id
    @Column(name = "katId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long katId;

    @Column(name = "standard")
    private String standard;

    @Column(name = "katIme")
    private String katIme;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bib_id", nullable = false)
    private Biblioteka biblioteka;

    @OneToMany(mappedBy = "katalog")
    private List<Knjiga> books = new ArrayList<>();

    private boolean deleted;

    public Katalog(Long katId, String standard, String katIme, Biblioteka biblioteka) {
        this.katId = katId;
        this.standard = standard;
        this.katIme = katIme;
        this.biblioteka = biblioteka;
        deleted = false;
    }

    public Katalog() {
    }

    public Long getKatId() {
        return katId;
    }

    public void setKatId(Long katId) {
        this.katId = katId;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getKatIme() {
        return katIme;
    }

    public void setKatIme(String katIme) {
        this.katIme = katIme;
    }

    public Biblioteka getBiblioteka() {
        return biblioteka;
    }

    public void setBiblioteka(Biblioteka biblioteka) {
        this.biblioteka = biblioteka;
    }

    public List<Knjiga> getBooks() {
        return books;
    }

    public void setBooks(List<Knjiga> books) {
        this.books = books;
    }
}
