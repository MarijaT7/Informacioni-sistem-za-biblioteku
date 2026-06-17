package ftn.iis.controller;

import ftn.iis.enums.Uloge;
import ftn.iis.service.SistemskePreporukeService;
import ftn.iis.utils.JwtService;
import io.jsonwebtoken.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/sistemske-preporuke")
public class SistemskePreporukeController {
    private final SistemskePreporukeService sistemskePreporukeService;
    private final JwtService jwtService;

    public SistemskePreporukeController(SistemskePreporukeService sistemskePreporukeService, JwtService jwtService){
        this.sistemskePreporukeService = sistemskePreporukeService;
        this.jwtService = jwtService;
    }

    @PostMapping("/pokreni-analizu")
    public ResponseEntity<?> pokreniAnalizu(@RequestHeader("Authorization") String authHeader ){
        String token = authHeader.substring(7);
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino menadzer moze da pokrece analizu trendova.");
        }
        sistemskePreporukeService.generisiPreporuke();
        return ResponseEntity.ok().build();
    }

}
