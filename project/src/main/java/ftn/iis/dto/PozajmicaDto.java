package ftn.iis.dto;

import ftn.iis.model.Pozajmica;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PozajmicaDto {
    private Long idP;
    private LocalDate datPoz;
    private LocalDate datVrac;
    private LocalDate datOcVrac;
    private Boolean statusPoz;
    private Long idPK;
    private String isbn;
    private String naslovKnjige;
    private String autorKnjige;
    private String putanjaNaslovna;
    private List<ProduzenjePozajmiceDto> produzenja;

    public PozajmicaDto() {}

    public static PozajmicaDto fromPozajmica(Pozajmica p) {
        PozajmicaDto dto = new PozajmicaDto();
        dto.idP = p.getIdP();
        dto.datPoz = p.getDatPoz();
        dto.datVrac = p.getDatVrac();
        dto.datOcVrac = p.getDatOcVrac();
        dto.statusPoz = p.getStatusPoz();
        dto.idPK = p.getPrimerakKnjige().getIdPK();
        dto.isbn = p.getPrimerakKnjige().getFizickaKnjiga().getIsbn();
        dto.naslovKnjige = p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov();
        dto.autorKnjige = p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getAutor();
        dto.putanjaNaslovna = p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getPutanjaNaslovna();
        dto.produzenja = p.getProduzenja() != null ? p.getProduzenja().stream()
                .map(ProduzenjePozajmiceDto::fromProduzenje)
                .collect(Collectors.toList()) : null;
        return dto;
    }

    public Long getIdP() { return idP; }
    public void setIdP(Long idP) { this.idP = idP; }
    public LocalDate getDatPoz() { return datPoz; }
    public void setDatPoz(LocalDate datPoz) { this.datPoz = datPoz; }
    public LocalDate getDatVrac() { return datVrac; }
    public void setDatVrac(LocalDate datVrac) { this.datVrac = datVrac; }
    public LocalDate getDatOcVrac() { return datOcVrac; }
    public void setDatOcVrac(LocalDate datOcVrac) { this.datOcVrac = datOcVrac; }
    public Boolean getStatusPoz() { return statusPoz; }
    public void setStatusPoz(Boolean statusPoz) { this.statusPoz = statusPoz; }
    public Long getIdPK() { return idPK; }
    public void setIdPK(Long idPK) { this.idPK = idPK; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getNaslovKnjige() { return naslovKnjige; }
    public void setNaslovKnjige(String naslovKnjige) { this.naslovKnjige = naslovKnjige; }
    public String getAutorKnjige() { return autorKnjige; }
    public void setAutorKnjige(String autorKnjige) { this.autorKnjige = autorKnjige; }
    public String getPutanjaNaslovna() { return putanjaNaslovna; }
    public void setPutanjaNaslovna(String putanjaNaslovna) { this.putanjaNaslovna = putanjaNaslovna; }
    public List<ProduzenjePozajmiceDto> getProduzenja() { return produzenja; }
    public void setProduzenja(List<ProduzenjePozajmiceDto> produzenja) { this.produzenja = produzenja; }
}
