package ftn.iis.dto;

public class OsnovniDobavljacDto {
    private String naziv;
    private String tel;

    public OsnovniDobavljacDto(String naziv, String tel){
        this.naziv = naziv;
        this.tel = tel;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getTel(){
        return tel;
    }
}
