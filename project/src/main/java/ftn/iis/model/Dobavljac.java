package ftn.iis.model;

import jakarta.persistence.*;

@Entity
@Table(name="dobavljac")
public class Dobavljac {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="naziv")
    private String naziv;
    @Column(name="email")
    private String email;
    @Column(name="telefon")
    private String tel;
    @Column(name="pib")
    private String pib;

    @OneToOne(mappedBy = "dobavljac", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Knjizara knjizara;

    @OneToOne(mappedBy = "dobavljac", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Izdavac izdavac;

    public Dobavljac(){

    }

    public Dobavljac(String naziv, String email, String tel, String pib){
        this.naziv = naziv;
        this.email = email;
        this.tel = tel;
        this.pib = pib;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPib() {
        return pib;
    }

    public void setPib(String pib) {
        this.pib = pib;
    }

}
