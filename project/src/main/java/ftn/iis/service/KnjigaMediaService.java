package ftn.iis.service;

import ftn.iis.model.AudioKnjiga;
import ftn.iis.model.EKnjiga;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.AudioKnjigaRepository;
import ftn.iis.repository.EKnjigaRepository;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class KnjigaMediaService {
    private final KnjigaRepository knjigaRepository;
    private final EKnjigaRepository eKnjigaRepository;
    private final AudioKnjigaRepository audioKnjigaRepository;

    public KnjigaMediaService(KnjigaRepository knjigaRepository, EKnjigaRepository eKnjigaRepository, AudioKnjigaRepository audioKnjigaRepository) {
        this.knjigaRepository = knjigaRepository;
        this.eKnjigaRepository = eKnjigaRepository;
        this.audioKnjigaRepository = audioKnjigaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Path> getNaslovnaPath(String isbn) {
        return knjigaRepository.findByIsbn(isbn)
                .filter(k -> !k.isDeleted())
                .map(Knjiga::getPutanjaNaslovna)
                .flatMap(this::resolvePath);
    }

    @Transactional(readOnly = true)
    public Optional<Path> getPdfPath(String isbn) {
        return eKnjigaRepository.findById(isbn)
                .map(EKnjiga::getPutanjaEK)
                .flatMap(this::resolvePath);
    }

    @Transactional(readOnly = true)
    public Optional<Path> getAudioPath(String isbn) {
        return audioKnjigaRepository.findById(isbn)
                .map(AudioKnjiga::getPutanjaAK)
                .flatMap(this::resolvePath);
    }

    private Optional<Path> resolvePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return Optional.empty();
        }

        Path path = Paths.get(rawPath).normalize().toAbsolutePath();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }

        return Optional.of(path);
    }
}
