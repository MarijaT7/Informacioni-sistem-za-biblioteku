package ftn.iis.controller;

import ftn.iis.dto.BookDto;
import ftn.iis.model.Knjiga;
import ftn.iis.service.KnjigaService;
import ftn.iis.utils.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/knjiga")
public class KnjigaController {

    private final KnjigaService knjigaService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;

    public KnjigaController(KnjigaService knjigaService, JwtService jwtService) {
        this.knjigaService = knjigaService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{isbn}")
    public BookDto getBookInfoByIsbn(@PathVariable String isbn){
        return knjigaService.getByISBN(isbn).get();
    }

    @GetMapping("/all")
    public List<BookDto> getAllBooks(){
        return knjigaService.getAll();
    }
    
}
