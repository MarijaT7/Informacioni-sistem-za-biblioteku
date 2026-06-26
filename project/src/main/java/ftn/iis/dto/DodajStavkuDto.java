package ftn.iis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DodajStavkuDto {

    @NotNull(message = "Knjiga je obavezna.")
    private String isbn;

    @NotNull(message = "Količina je obavezna.")
    @Min(value = 1, message = "Količina mora biti najmanje 1.")
    private Integer kolicina;

    @NotNull(message = "Okvirna cena je obavezna.")
    @DecimalMin(value = "0.01", message = "Cena mora biti veća od 0.")
    private Double okvirnaCena;

    private Long predlogId;

    private Long preporukaId;

}