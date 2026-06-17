package ftn.iis.dto;

import ftn.iis.enums.StatusSistemskePreporuke;

import java.time.LocalDateTime;

public class SistemskaPreporukaResponseDto {

    private Long id;
    private String isbn;
    private String naslov;
    private String autor;
    private Integer trenutniBrojPrimeraka;
    private Integer brojPozajmica;
    private LocalDateTime datumGenerisanja;
    private String predlog;
    private StatusSistemskePreporuke status;

    public SistemskaPreporukaResponseDto() {}

    public SistemskaPreporukaResponseDto(Long id, String naslov, Integer trenutniBrojPrimeraka, Integer brojPozajmica,
                                         String predlog, String isbn, String autor) {
        this.id = id;
        this.isbn = isbn;
        this.naslov = naslov;
        this.autor = autor;
        this.trenutniBrojPrimeraka = trenutniBrojPrimeraka;
        this.brojPozajmica = brojPozajmica;
        this.predlog = predlog;
        this.datumGenerisanja = LocalDateTime.now();
        this.status = StatusSistemskePreporuke.AKTIVNA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public Integer getTrenutniBrojPrimeraka() {
        return trenutniBrojPrimeraka;
    }

    public void setTrenutniBrojPrimeraka(Integer trenutniBrojPrimeraka) {
        this.trenutniBrojPrimeraka = trenutniBrojPrimeraka;
    }

    public Integer getBrojPozajmica() {
        return brojPozajmica;
    }

    public void setBrojPozajmica(Integer brojPozajmica) {
        this.brojPozajmica = brojPozajmica;
    }

    public String getPredlog() {
        return predlog;
    }

    public void setPredlog(String predlog) {
        this.predlog = predlog;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDateTime getDatumGenerisanja() {
        return datumGenerisanja;
    }

    public void setDatumGenerisanja(LocalDateTime datumGenerisanja) {
        this.datumGenerisanja = datumGenerisanja;
    }

    public StatusSistemskePreporuke getStatus() {
        return status;
    }

    public void setStatus(StatusSistemskePreporuke status) {
        this.status = status;
    }
}