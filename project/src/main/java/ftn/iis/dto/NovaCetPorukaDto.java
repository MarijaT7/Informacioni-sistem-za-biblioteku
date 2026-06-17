package ftn.iis.dto;

public class NovaCetPorukaDto {
    private String sadrzajPoruke;

    public NovaCetPorukaDto() {
    }

    public NovaCetPorukaDto(String sadrzajPoruke) {
        this.sadrzajPoruke = sadrzajPoruke;
    }

    public String getSadrzajPoruke() {
        return sadrzajPoruke;
    }

    public void setSadrzajPoruke(String sadrzajPoruke) {
        this.sadrzajPoruke = sadrzajPoruke;
    }
}
