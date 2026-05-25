package ftn.iis.controller;

import ftn.iis.dto.BookDto;
import ftn.iis.dto.CitanjeProgressDto;
import ftn.iis.dto.KnjigaDetaljiDto;
import ftn.iis.dto.KnjigaOsnovnoDto;
import ftn.iis.dto.NewBookDto;
import ftn.iis.dto.SlusanjeProgressDto;
import ftn.iis.model.Knjiga;
import ftn.iis.service.KatalogService;
import ftn.iis.service.KnjigaMediaService;
import ftn.iis.service.KnjigaProgressService;
import ftn.iis.service.KnjigaService;
import ftn.iis.utils.JwtService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.lang.annotation.Repeatable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/knjiga")
public class KnjigaController {

    private final KnjigaService knjigaService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;
    private final KatalogService katalogService;
    private final KnjigaMediaService knjigaMediaService;
    private final KnjigaProgressService knjigaProgressService;

    public KnjigaController(KnjigaService knjigaService, JwtService jwtService, KatalogService katalogService, KnjigaMediaService knjigaMediaService, KnjigaProgressService knjigaProgressService) {
        this.knjigaService = knjigaService;
        this.jwtService = jwtService;
        this.katalogService = katalogService;
        this.knjigaMediaService = knjigaMediaService;
        this.knjigaProgressService = knjigaProgressService;
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
        if(k == null)
            return ResponseEntity.ok("Knjiga ne postoji ili je vec obrisana");
        k.setDeleted(true);
        knjigaService.saveBook(k);
        return ResponseEntity.ok("Uspesno obrisana knjiga");
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<?> updateBook(@RequestHeader("Authorization") String authHeader, @PathVariable String isbn, @RequestBody BookDto bookDto){
        //This is like the 8th copy and paste of this block man tech debt is huge with this one
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze praviti kataloge");

        Knjiga k = knjigaService.getBookByISBN(isbn);
        if(k == null)
            return ResponseEntity.ok("Ne postoji ta knjiga");
        k.setNaslov(bookDto.getNaziv());
        k.setAutor(bookDto.getAutor());
        k.setSinopsis(bookDto.getSinopsis());
        knjigaService.saveBook(k);
        return ResponseEntity.ok("Uspesno azurirana knjiga");
    }

    @GetMapping("/sve/osnovno")
    public ResponseEntity<List<KnjigaOsnovnoDto>> ispisiSveKnjige(@RequestHeader(value = "Authorization", required = false) String authHeader){
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<KnjigaOsnovnoDto> rezultat = knjigaService.ispisiSveKnjige();
        return ResponseEntity.ok(rezultat);
    }

    @GetMapping("/pretraga")
    public ResponseEntity<List<KnjigaOsnovnoDto>> pretraziKnjige(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "q", required = false) String query
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(knjigaService.ispisiSveKnjige());
        }

        return ResponseEntity.ok(knjigaService.pretraziPoNaslovu(query));
    }

    @GetMapping("/detalji/{isbn}")
    public ResponseEntity<KnjigaDetaljiDto> detaljiKnjige(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaService.getDetaljiByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/naslovna/{isbn}")
    public ResponseEntity<Resource> naslovnaKnjige(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaMediaService.getNaslovnaPath(isbn)
                .map(this::fileResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/eknjiga/{isbn}/pdf")
    public ResponseEntity<Resource> preuzmiPdf(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaMediaService.getPdfPath(isbn)
                .map(this::fileResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/audioknjiga/{isbn}/audio")
    public ResponseEntity<Resource> preuzmiAudio(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaMediaService.getAudioPath(isbn)
                .map(this::fileResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/eknjiga/{isbn}/progress")
    public ResponseEntity<CitanjeProgressDto> getCitanjeProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn
    ) {
        String role = safeExtractRole(authHeader);
        String jmbg = safeExtractJmbg(authHeader);
        if (role == null || jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaProgressService.getCitanjeProgress(jmbg, isbn)
                .map(val -> ResponseEntity.ok(new CitanjeProgressDto(val)))
                .orElseGet(() -> ResponseEntity.ok(new CitanjeProgressDto(1)));
    }

    @PutMapping("/eknjiga/{isbn}/progress")
    public ResponseEntity<CitanjeProgressDto> sacuvajCitanjeProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn,
            @RequestBody CitanjeProgressDto request
    ) {
        String role = safeExtractRole(authHeader);
        String jmbg = safeExtractJmbg(authHeader);
        if (role == null || jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaProgressService.sacuvajCitanje(jmbg, isbn, request.getTrenutnaStranica(), request.isZavrseno())
                .map(val -> ResponseEntity.ok(new CitanjeProgressDto(val)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/audioknjiga/{isbn}/progress")
    public ResponseEntity<SlusanjeProgressDto> getSlusanjeProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn
    ) {
        String role = safeExtractRole(authHeader);
        String jmbg = safeExtractJmbg(authHeader);
        if (role == null || jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaProgressService.getSlusanjeProgress(jmbg, isbn)
                .map(val -> ResponseEntity.ok(new SlusanjeProgressDto(val)))
                .orElseGet(() -> ResponseEntity.ok(new SlusanjeProgressDto(0)));
    }

    @PutMapping("/audioknjiga/{isbn}/progress")
    public ResponseEntity<SlusanjeProgressDto> sacuvajSlusanjeProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String isbn,
            @RequestBody SlusanjeProgressDto request
    ) {
        String role = safeExtractRole(authHeader);
        String jmbg = safeExtractJmbg(authHeader);
        if (role == null || jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return knjigaProgressService.sacuvajSlusanje(jmbg, isbn, request.getTrenutnaSekunda(), request.isZavrseno())
                .map(val -> ResponseEntity.ok(new SlusanjeProgressDto(val)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private String safeExtractRole(String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return null;
            }
            return jwtService.extractRole(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeExtractJmbg(String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return null;
            }
            return jwtService.extractJmbg(token);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRoleAllowed(String role, String... allowedRoles) {
        if (role == null) {
            return false;
        }
        for (String allowed : allowedRoles) {
            if (role.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    private ResponseEntity<Resource> fileResponse(Path path) {
        MediaType mediaType = MediaTypeFactory.getMediaType(path.getFileName().toString())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(path));
    }

    /* korisno za neke metode
    String token = authHeader.substring(7);
    String role = jwtService.extractRole(token);
        if (!role.equalsIgnoreCase("CLAN") && !role.equalsIgnoreCase("BIBLIOTEKAR")) {
            throw new OtherRoleSearchesBooksException();
        }
     */
}
