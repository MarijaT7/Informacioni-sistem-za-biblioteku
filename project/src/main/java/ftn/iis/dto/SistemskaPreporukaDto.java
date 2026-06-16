package ftn.iis.dto;

public class SistemskaPreporukaDto {

    private Long id;

    private String naslov;

    private Integer trenutniBrojPrimeraka;

    private Integer brojPozajmica;

    private Integer brojRezervacija;

    private String predlog;

    public SistemskaPreporukaDto(Long id, String naslov, Integer trenutniBrojPrimeraka, Integer brojPozajmica, Integer brojRezervacija, String predlog) {
        this.id = id;
        this.naslov = naslov;
        this.trenutniBrojPrimeraka = trenutniBrojPrimeraka;
        this.brojPozajmica = brojPozajmica;
        this.brojRezervacija = brojRezervacija;
        this.predlog = predlog;
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

    public Integer getBrojRezervacija() {
        return brojRezervacija;
    }

    public void setBrojRezervacija(Integer brojRezervacija) {
        this.brojRezervacija = brojRezervacija;
    }

    public String getPredlog() {
        return predlog;
    }

    public void setPredlog(String predlog) {
        this.predlog = predlog;
    }
}