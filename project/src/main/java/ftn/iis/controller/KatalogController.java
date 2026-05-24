package ftn.iis.controller;

import ftn.iis.dto.KatalogDto;
import ftn.iis.model.Katalog;
import ftn.iis.model.User;
import ftn.iis.service.KatalogService;
import ftn.iis.service.UserService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/katalog")
public class KatalogController {
    private static final int BEARER_PREFIX_LENGTH = 7;
    private final KatalogService katalogService;
    private final JwtService jwtService;
    private final UserService userService;

    public KatalogController(KatalogService katalogService, JwtService jwtService, UserService userService) {
        this.katalogService = katalogService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/novi")
    public ResponseEntity<?> newCatalog(@RequestHeader("Authorization") String authHeader, @RequestBody KatalogDto katalogDto){
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze praviti kataloge");

        Optional<User> user = userService.getUserByJmbg(jmbg);
        Katalog kat = new Katalog();

        kat.setKatIme(katalogDto.getNaziv());
        kat.setStandard(katalogDto.getStandard());
        kat.setBiblioteka(user.get().getBiblioteka());

        if(katalogService.addNewCatalog(kat))
            return ResponseEntity.ok(katalogDto);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add catalog");
    }

}
