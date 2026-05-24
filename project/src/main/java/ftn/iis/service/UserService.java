package ftn.iis.service;

import ftn.iis.model.User;
import ftn.iis.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByJmbg(String jmbg){
        return userRepository.findByJmbg(jmbg);
    }
}
