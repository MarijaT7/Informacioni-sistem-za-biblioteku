package ftn.iis.model;

import ftn.iis.enums.StatusSistemskePreporuke;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sistemska_preporuka")
public class SistemskaPreporuka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "isbn")
    private FizickaKnjiga fizickaKnjiga;

    private Integer brojPozajmica;

    private Integer brojRezervacija;

    private Integer trenutniBrojPrimeraka;

    private String predlog;

    private LocalDateTime datumGenerisanja;

    @Enumerated(EnumType.STRING)
    private StatusSistemskePreporuke status;
}