package ftn.iis.service;

import ftn.iis.dto.AuthResponse;
import ftn.iis.dto.Login;
import ftn.iis.model.User;
import ftn.iis.repository.*;
import ftn.iis.utils.JwtService;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BibliotekaRepository bibliotekaRepository;
    private final KategorijaClanaRepository kategorijaClanaRepository;
    private final ClanarinaRepository clanarinaRepository;
    private final GenreRepository genreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, BibliotekaRepository bibliotekaRepository, KategorijaClanaRepository kategorijaClanaRepository, ClanarinaRepository clanarinaRepository, GenreRepository genreRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.bibliotekaRepository = bibliotekaRepository;
        this.kategorijaClanaRepository = kategorijaClanaRepository;
        this.clanarinaRepository = clanarinaRepository;
        this.genreRepository = genreRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    //logovanje korisnika
    public AuthResponse login(Login request){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())

        );
        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Korisnik nije pronadjen"));
        return buildAuthResponse(user);
    }
    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setJmbg(user.getJmbg());
        res.setEmail(user.getEmail());
        res.setRole(user.getUloge().name());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        return res;
    }

}
