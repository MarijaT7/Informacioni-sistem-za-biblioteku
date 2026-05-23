package ftn.iis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "izdavac")
public class Izdavac {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Dobavljac dobavljac;

    public Izdavac(){}

    public Izdavac(Dobavljac dobavljac){
        this.dobavljac = dobavljac;
    }
}
