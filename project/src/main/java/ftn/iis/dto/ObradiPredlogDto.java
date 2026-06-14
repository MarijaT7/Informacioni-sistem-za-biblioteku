package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;

public class ObradiPredlogDto {
    @NotBlank(message = "Status je obavezan.")
    private String status;

    private String obrazlozenje;

    public ObradiPredlogDto() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObrazlozenje() {
        return obrazlozenje;
    }

    public void setObrazlozenje(String obrazlozenje) {
        this.obrazlozenje = obrazlozenje;
    }
}
