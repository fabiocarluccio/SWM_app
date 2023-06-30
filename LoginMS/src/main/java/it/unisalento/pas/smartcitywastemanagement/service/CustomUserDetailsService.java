package it.unisalento.pas.smartcitywastemanagement.service;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.exceptions.UserNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service // va taggato come Service in modo che spring lo possa vedere all'avvio
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository; // bisogna usare il repository per trovare gli utenti da autenticare

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<User> optUser = userRepository.findByUsername(username);

        if (!optUser.isPresent()) {
            throw new UsernameNotFoundException(username);
        }

        final User user = optUser.get();

        // TODO - qua si implementa logica che assegna ruoli diversi a seconda del ruolo dell'utente

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername()).password(user.getPassword()).roles("USER").build();

        return userDetails;
    }
}
