package ftn.iis.service;

import ftn.iis.repository.SistemskePreporukeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SistemskePreporukeService {
    private final SistemskePreporukeRepository sistemskePreporukeRepository;

    public SistemskePreporukeService(SistemskePreporukeRepository sistemskePreporukeRepository) {
        this.sistemskePreporukeRepository = sistemskePreporukeRepository;
    }

    // rucno pokretanje analize trendova
    public void generisiPreporuke () {

    }

    // automatsko pokretanje analize svakog ponedeljka u ponoc ~~~
    @Scheduled(cron = "0 0 0 * * MON")
    public void automatskaAnaliza() {

        generisiPreporuke();

    }

}
