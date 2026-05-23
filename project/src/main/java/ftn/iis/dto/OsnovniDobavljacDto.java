package ftn.iis.dto;

import ftn.iis.enums.StatusDobavljaca;

public class OsnovniDobavljacDto {
    private String naziv;
    private StatusDobavljaca status;
    private String tel;

    public OsnovniDobavljacDto(String naziv, String tel, StatusDobavljaca status){
        this.naziv = naziv;
        this.tel = tel;
        this.status = status;
    }

    public StatusDobavljaca getStatus() {
        return status;
    }

    public void setStatus(StatusDobavljaca status) {
        this.status = status;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
