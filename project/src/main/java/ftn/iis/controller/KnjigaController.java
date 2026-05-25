package ftn.iis.controller;

import ftn.iis.dto.BookDto;
import ftn.iis.dto.NewBookDto;
import ftn.iis.model.Knjiga;
import ftn.iis.service.KatalogService;
import ftn.iis.service.KnjigaService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.lang.annotation.Repeatable;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/knjiga")
public class KnjigaController {

    private final KnjigaService knjigaService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;
    private final KatalogService katalogService;

    public KnjigaController(KnjigaService knjigaService, JwtService jwtService, KatalogService katalogService) {
        this.knjigaService = knjigaService;
        this.jwtService = jwtService;
        this.katalogService = katalogService;
    }

    @GetMapping("/{isbn}")
    public BookDto getBookInfoByIsbn(@PathVariable String isbn){
        return knjigaService.getByISBN(isbn).get();
    }

    @GetMapping("/all")
    public List<BookDto> getAllBooks(){
        return knjigaService.getAll();
    }

    @PostMapping("/nova")
    public ResponseEntity<?> postNewBook(@RequestHeader("Authorization") String authHeader, @RequestBody NewBookDto newBookDto){
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze praviti kataloge");

        Knjiga k = new Knjiga();
        k.setIsbn(newBookDto.getIsbn());
        k.setAutor(newBookDto.getAutor());
        k.setNaslov(newBookDto.getNaziv());
        k.setSinopsis(newBookDto.getSinopsis());
        k.setKatalog(katalogService.getByKatId(newBookDto.getKatId()).get());

        try {
            knjigaService.newBook(k);
            return ResponseEntity.ok("Uspesno dodata knjiga");
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add new book");
        }
    }

    @PutMapping("delete/{isbn}")
    public ResponseEntity<?> deleteBook(@RequestHeader("Authorization") String authHeader, @PathVariable String isbn){
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze praviti kataloge");

        Knjiga k = knjigaService.getBookByISBN(isbn);
        k.setDeleted(true);
        knjigaService.saveBook(k);
        return ResponseEntity.ok("Uspesno obrisana knjiga");
    }
}
