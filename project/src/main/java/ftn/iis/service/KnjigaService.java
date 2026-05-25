package ftn.iis.service;

import ftn.iis.dto.BookDto;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KnjigaService {
    private final KnjigaRepository knjigaRepository;

    public KnjigaService(KnjigaRepository knjigaRepository) {
        this.knjigaRepository = knjigaRepository;
    }

    public Optional<BookDto> getByISBN(String isbn){
        Optional<Knjiga> k = knjigaRepository.findByIsbn(isbn);
        if(k == null)
            return null;

        BookDto bookDto = new BookDto(k.get());
        return  Optional.of(bookDto);
    }

    public List<BookDto> getAll(){
        List<Knjiga> books = knjigaRepository.findAll();
        List<BookDto> retBooks = new ArrayList<>();
        for(Knjiga k: books){
            retBooks.add(new BookDto(k));
        }

        return retBooks;
    }

    public void newBook(Knjiga knjiga){
        knjigaRepository.saveAndFlush(knjiga);
    }
}
