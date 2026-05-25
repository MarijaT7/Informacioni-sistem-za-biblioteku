package ftn.iis.service;

import ftn.iis.dto.BookDto;
import ftn.iis.dto.KnjigaDetaljiDto;
import ftn.iis.dto.KnjigaOsnovnoDto;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KnjigaService {
    private final KnjigaRepository knjigaRepository;

    public KnjigaService(KnjigaRepository knjigaRepository) {
        this.knjigaRepository = knjigaRepository;
    }

    public Optional<BookDto> getByISBN(String isbn){
        Optional<Knjiga> k = knjigaRepository.findByIsbn(isbn);
        if(k == null || k.get().isDeleted())
            return null;

        BookDto bookDto = new BookDto(k.get());
        return  Optional.of(bookDto);
    }

    public Knjiga getBookByISBN(String isbn){
        Knjiga k = knjigaRepository.findByIsbn(isbn).get();
        if(k.isDeleted())
            return null;
        return k;
    }

    public List<BookDto> getAll(){
        List<Knjiga> books = knjigaRepository.findAll();
        List<BookDto> retBooks = new ArrayList<>();
        for(Knjiga k: books){
            if(!k.isDeleted())
                retBooks.add(new BookDto(k));
        }

        return retBooks;
    }

    public void newBook(Knjiga knjiga){
        knjigaRepository.saveAndFlush(knjiga);
    }
    //DO NOT ASK ME WHAT DIFFERENCE THERE IS BETWEEN THESE TWO OK I LIKE NAMING THINGS
    public void saveBook(Knjiga knjiga) {
        knjigaRepository.saveAndFlush(knjiga);
    }

    public List<KnjigaOsnovnoDto> ispisiSveKnjige() {
        List<Knjiga> knjige = knjigaRepository.findByDeletedFalse();
        return knjige.stream().map(KnjigaOsnovnoDto::fromKnjiga).collect(Collectors.toList());
    }

    public List<KnjigaOsnovnoDto> pretraziPoNaslovu(String query) {
        List<Knjiga> knjige = knjigaRepository.findByNaslovContainingIgnoreCaseAndDeletedFalse(query);
        return knjige.stream().map(KnjigaOsnovnoDto::fromKnjiga).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<KnjigaDetaljiDto> getDetaljiByIsbn(String isbn) {
        return knjigaRepository.findByIsbn(isbn)
                .filter(k -> !k.isDeleted())
                .map(KnjigaDetaljiDto::fromKnjiga);
    }
}
