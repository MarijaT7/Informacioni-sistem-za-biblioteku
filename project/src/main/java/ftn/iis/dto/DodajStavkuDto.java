package ftn.iis.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DodajStavkuDto {

    // ISBN vise nije obavezan !!
    private String isbn;

    @NotNull(message = "Količina je obavezna.")
    @Min(value = 1, message = "Količina mora biti najmanje 1.")
    private Integer kolicina;

    private Long predlogId;

    private Long preporukaId;

    // Potrebno je da stavka ima jedno od troje
    @AssertTrue(message = "Stavka mora imati isbn, predlogId ili preporukaId.")
    public boolean isIzvorPostavljen() {
        return isbn != null || predlogId != null || preporukaId != null;
    }

}