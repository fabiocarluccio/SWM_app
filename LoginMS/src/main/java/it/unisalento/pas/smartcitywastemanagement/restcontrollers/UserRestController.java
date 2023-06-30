package it.unisalento.pas.smartcitywastemanagement.restcontrollers;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.dto.AuthenticationResponseDTO;
import it.unisalento.pas.smartcitywastemanagement.dto.LoginDTO;
import it.unisalento.pas.smartcitywastemanagement.dto.UserDTO;
import it.unisalento.pas.smartcitywastemanagement.exceptions.UserNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.repositories.UserRepository;
import it.unisalento.pas.smartcitywastemanagement.security.JwtUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unisalento.pas.smartcitywastemanagement.configuration.SecurityConfig.passwordEncoder;

@RestController
@RequestMapping("/api/users")
public class UserRestController { // va a gestire tutto il ciclo CRUD degli utenti
    @Autowired
    UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;

    @RequestMapping(value="/registration", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO registration(@RequestBody UserDTO userDTO) {

        User newUser = new User();
        newUser.setName(userDTO.getName());
        newUser.setSurname(userDTO.getSurname());
        newUser.setEmail(userDTO.getEmail());
        newUser.setAge(userDTO.getAge());
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordEncoder().encode(userDTO.getPassword()));
        newUser.setRole(userDTO.getRole());

        // salvo utente nel db
        newUser = userRepository.save(newUser);
        System.out.println("L'ID DEL NUOVO UTENTE E'"+newUser.getId());

        // restituisco l'utente aggiunto nel db curandomi del fatto di rimuovere la password (per sicurezza)
        userDTO.setId(newUser.getId());
        userDTO.setPassword(null);

        return userDTO;
    }

    @RequestMapping(value="/", method= RequestMethod.GET)
    public List<UserDTO> getAll() {
        List<UserDTO> utenti = new ArrayList<>();

        for(User user : userRepository.findAll()) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setSurname(user.getSurname());
            userDTO.setEmail(user.getEmail());
            userDTO.setAge(user.getAge());
            userDTO.setUsername(user.getUsername());

            utenti.add(userDTO);
        }

        return utenti;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public UserDTO get(@PathVariable String id) throws UserNotFoundException {
        System.out.println("ARRIVATO L'ID: " + id);

        Optional<User> optUser = userRepository.findById(id);

        if (optUser.isPresent()) {
            User user = optUser.get();

            UserDTO user1 = new UserDTO();
            user1.setName(user.getName());
            user1.setSurname(user.getSurname());
            user1.setEmail(user.getEmail());
            user1.setAge(user.getAge());
            user1.setId(user.getId());

            return user1;
        }
        throw new UserNotFoundException();
    }



    @RequestMapping(value="/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO post(@RequestBody UserDTO userDTO) {

        User newUser = new User();
        newUser.setName(userDTO.getName());
        newUser.setSurname(userDTO.getSurname());
        newUser.setEmail(userDTO.getEmail());
        newUser.setAge(userDTO.getAge());
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordEncoder().encode(userDTO.getPassword()));

        // salvo utente nel db
        newUser = userRepository.save(newUser);
        System.out.println("L'ID DEL NUOVO UTENTE E'"+newUser.getId());

        // restituisco l'utente aggiunto nel db curandomi del fatto di rimuovere la password (per sicurezza)
        userDTO.setId(newUser.getId());
        userDTO.setPassword(null);

        return userDTO;
    }

    @RequestMapping(value="/find", method = RequestMethod.GET)
    public List<UserDTO> findBySurname(@RequestParam String surname) {
        List<User> result = userRepository.findBySurname(surname);  // Da un lato interrogo il db
        List<UserDTO> utenti = new ArrayList<>();                   // Dall'altro mi creo la lista di risultati
                                                                    // (che devono essere necesariamente DTO).

        // e poi trasformo le classi di domain dentro classi DTO
        for(User user: result) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setSurname(user.getSurname());
            userDTO.setEmail(user.getEmail());
            userDTO.setAge(user.getAge());

            utenti.add(userDTO);
        }

        // Restituisco la lista dei DTO
        return utenti;
    }

    @RequestMapping(value="/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) {

        // Chiedo al framework di spring se l'utente con le credenziali loginDTO è autorizzato oppure no
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
        );

        // Ora che abbiamo fatto l'autenticazione con il motore di spring, torniamo ora nel dominio della nostra applicazione.
        // quindi carichiamo l'utente attraverso il repository.
        // È buona norma prendersi tutti i dati dell'utente
        // ad esempio mi prendo il campo mail in modo che mando la mail una volta che lui ha eseguito l'accesso (o tentato di farlo)
        // per cui carichiamolo in memoria
        User user = userRepository.findByUsername(authentication.getName()); // avremmo potuto prendere lo username anche da loginDTO invece da authentication, fa lo stesso visto che lo abbiamo caricato in authentication da loginDTO

        if(user == null) { // sto controllo lo ha messo "perche non si sa mai" (lezione 15, 1.20.00)
            throw new UsernameNotFoundException(loginDTO.getUsername());
        }

        // Questo è un oggetto al cui sto dicendo che, da questo momento in poi, se sto richiamando la logica di business,
        // questo utente è autenticato.
        // Attenzione che questa autenticazione vale a livello di thread. Significa che se io poi rifaccio una altra chiamata
        // api dopo, il context è svuotato! Insomma: se dopo arriva una chiamata di un utente diverso, il context deve essere
        // svuotato, altrimenti quell'utente avrebbe il context di un altro utente.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // quindi se io qua sotto chiamo della business logic, i metodi della business logic potranno accedere al SecurityContext,
        // in modo da sapere chi è l'utente che ha fatto questa richiesta.

        // ... eventuale logica di business

        // genero token jwt
        final String jwt = jwtUtilities.generateToken(user.getUsername());

        // rispondo alla chiamata api con il token jwt generato
        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));

    }
}
