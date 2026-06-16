package ftn.iis.controller;

import ftn.iis.service.SistemskePreporukeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sistemske-preporuke")
public class SistemskePreporukeController {
    private final SistemskePreporukeService sistemskePreporukeService;

    public SistemskePreporukeController(SistemskePreporukeService sistemskePreporukeService){
        this.sistemskePreporukeService = sistemskePreporukeService;
    }

    @PostMapping("/pokreni-analizu")
    public ResponseEntity<?> pokreniAnalizu(@RequestHeader("Authorization") String authHeader ){


        sistemskePreporukeService.generisiPreporuke();
        return ResponseEntity.ok().build();
    }

}
