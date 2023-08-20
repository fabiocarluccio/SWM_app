package it.unisalento.pas.smartcitywastemanagement.restcontrollers;

import it.unisalento.pas.smartcitywastemanagement.domain.CitizenToken;
import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.dto.*;
import it.unisalento.pas.smartcitywastemanagement.exceptions.PasswordNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.TokenNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.UserNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.repositories.CitizenTokenRepository;
import it.unisalento.pas.smartcitywastemanagement.repositories.UserRepository;
import it.unisalento.pas.smartcitywastemanagement.security.JwtUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unisalento.pas.smartcitywastemanagement.configuration.SecurityConfig.passwordEncoder;

@CrossOrigin
@RestController
@RequestMapping("/api/authentication")
public class UserRestController { // va a gestire tutto il ciclo CRUD degli utenti
    @Autowired
    UserRepository userRepository;

    @Autowired
    CitizenTokenRepository citizenTokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;

    /**
     * Endpoint usato dall'Admin per registrare nuovi User.
     * Gli User possono avere uno di 4 ruoli: Citizen, MunicipalOffice, WasteManagementCompany, Admin.
     *
     * @param loginDTO
     * @return new User
     */
    //@PreAuthorize("hasRole('Admin')")
    @RequestMapping(value="/registration", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginDTO registration(@RequestBody LoginDTO loginDTO) {

        User newUser = new User();
        newUser.setEmail(loginDTO.getEmail());
        newUser.setUsername(loginDTO.getUsername());
        newUser.setPassword(passwordEncoder().encode(loginDTO.getPassword()));
        newUser.setRole(loginDTO.getRole());

        // salvo utente nel db
        newUser = userRepository.save(newUser);
        System.out.println("L'ID DEL NUOVO UTENTE E'"+newUser.getId());

        // restituisco l'utente aggiunto nel db curandomi del fatto di rimuovere la password (per sicurezza)
        loginDTO.setId(newUser.getId());
        loginDTO.setPassword(null);

        return loginDTO;
    }

    /**
     * Endpoint usato dall'Ufficio Comunale per registrare nuovi User con ruolo di "Cittadino".
     * Non è possibile inserire User aventi altri ruoli.
     * Viene aggiunto anche il token del cittadino nel database.
     *
     * @param citizenRegistrationDTO
     * @return new User
     */
    //@PreAuthorize("hasRole('MunicipalOffice')")
    @RequestMapping(value="/citizen_registration", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String citizenRegistration(@RequestBody CitizenRegistrationDTO citizenRegistrationDTO) {

        User newUser = new User();
        newUser.setEmail(citizenRegistrationDTO.getEmail());
        newUser.setUsername(citizenRegistrationDTO.getEmail());
        String password = User.generatePassword();
        System.out.println("Password: " + password);
        newUser.setPassword(passwordEncoder().encode(password));
        newUser.setRole("Citizen");

        CitizenToken newCitizenToken = new CitizenToken();
        newCitizenToken.setCitizenId(citizenRegistrationDTO.getCitizenId());

        // salvo utente nel db
        newUser = userRepository.save(newUser);
        System.out.println("L'ID DEL NUOVO UTENTE E'"+newUser.getId());

        // salvo token nel db
        newCitizenToken = citizenTokenRepository.save(newCitizenToken);
        System.out.println("IL TOKEN DEL NUOVO CITTADINO E'"+newCitizenToken.getToken());

        //TODO - eventuale invio tramite mail di username (e password eventualmente, dato che la si può reimpostare)

        // restituisco id user creato
        return newUser.getId();
    }

    /**
     * Endpoint usato da qualsiasi User per aggiornare la password.
     * Contiene due differenti logiche, a seconda di come PasswordResetDTO è configurato.
     * Aggiorna password usando come prova di autenticazione la vecchia password.
     * TODO - Per chiamare questo metodo è necessario che l'utente sia loggato (ovvero che faccia la richiesta mediante token jwt)
     *
     * @param passwordResetDTO
     * @throws PasswordNotMatchingException
     * @throws UserNotFoundException
     */
    @RequestMapping(value="/password_update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void passwordUpdate(@RequestBody PasswordResetDTO passwordResetDTO) throws PasswordNotMatchingException, UserNotFoundException {
        if (passwordResetDTO.getId() == null) {
            throw new UserNotFoundException();
        }

        User user = null;

        Optional<User> optUser = userRepository.findById(passwordResetDTO.getId());

        if (!optUser.isPresent()) {
            throw new UserNotFoundException();
        }

        user = optUser.get();

        // TODO - UnauthorizedUserException se l'utente che manda la richiesta sta cercando di cambiare la password di un altro utente

        // If old pwd doesn't correspond
        if (!passwordEncoder().matches(passwordResetDTO.getOldPassword(), user.getPassword())) {
            throw new PasswordNotMatchingException();
        }

        // Check new password requisites
        // TODO .. controlli che la nuova password rispetti i requisiti

        // Update password
        user.setPasswordResetToken(null);
        user.setPassword(passwordEncoder().encode(passwordResetDTO.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Endpoint usato da qualsiasi User per aggiornare la password.
     * Aggiorna password usando come prova di autenticazione il Token ricevuto via mail.
     *
     * @param passwordResetDTO
     * @throws TokenNotMatchingException
     * @throws UserNotFoundException
     */
    @RequestMapping(value="/password_reset", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void passwordReset(@RequestBody PasswordResetDTO passwordResetDTO) throws TokenNotMatchingException, UserNotFoundException {
        if (passwordResetDTO.getUsername() == null) {
            throw new UserNotFoundException();
        }

        User user = null;

        Optional<User> optUser = userRepository.findByUsername(passwordResetDTO.getUsername());

        if (optUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        user = optUser.get();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // If email Token doesn't correspond
        if (!passwordEncoder.matches(passwordResetDTO.getPasswordResetToken(), user.getPasswordResetToken())) {
            throw new TokenNotMatchingException();
        }

        // Check new password requisites
        // TODO .. controlli che la nuova password rispetti i requisiti

        // Update password
        user.setPasswordResetToken(null);
        user.setPassword(passwordEncoder().encode(passwordResetDTO.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Endpoint usato da qualsiasi user per reimpostare la password.
     * Invia mail contenente il Token che permette di reimpostare la password.
     *
     * @throws UserNotFoundException
     */
    @RequestMapping(value="/password_reset_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void passwordResetToken(@RequestBody LoginDTO loginCredentials) throws UserNotFoundException {

        // Carico utente
        Optional<User> optUser = userRepository.findByUsername(loginCredentials.getUsername());

        if (optUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        User user = optUser.get();

        // Genero token per reset password
        String token = User.generatePasswordResetToken();

        // Aggiungo token in db (user)
        user.setPasswordResetToken(passwordEncoder().encode(token));
        userRepository.save(user);

        // TODO - invio mail con token reset password
        // ...
        System.out.println("Token reset password: " + token);

    }

    /**
     * Endpoint di autenticazione.
     * Restituisce token JWT a client in modo che non debba inviare ripetutamente username e password ad ogni richiesta.
     *
     * @param loginDTO
     * @return Token JWT
     */
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
        Optional<User> optUser = userRepository.findByUsername(authentication.getName()); // avremmo potuto prendere lo username anche da loginDTO invece da authentication, fa lo stesso visto che lo abbiamo caricato in authentication da loginDTO

        if (!optUser.isPresent()) { // sto controllo lo ha messo "perche non si sa mai" (lezione 15, 1.20.00)
            throw new UsernameNotFoundException("L'account " + loginDTO.getUsername() + "non è esistente.");
        }

        User user = optUser.get();

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


    //@PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/get_info/{username}", method=RequestMethod.GET)
    public LoginDTO getInfo(@PathVariable String username) throws UserNotFoundException {
        System.out.println("Username: " + username);

        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            User user = optUser.get();

            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(user.getUsername());
            loginDTO.setEmail(user.getEmail());
            loginDTO.setRole(user.getRole());

            return loginDTO;
        }
        throw new UserNotFoundException();
    }

    @RequestMapping(value="/getall", method= RequestMethod.GET)
    public List<LoginDTO> getAll() {
        List<LoginDTO> utenti = new ArrayList<>();

        for(User user : userRepository.findAll()) {
            LoginDTO userDTO = new LoginDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setPassword(user.getPassword());
            userDTO.setRole(user.getRole());

            utenti.add(userDTO);
        }

        return utenti;
    }

    /**
     * Endpoint usato per ricavare l'id del cittadino, dato il token.
     * Utilizzato per autorizzare i conferimenti.
     *
     * @param citizen_token
     * @return
     * @throws UserNotFoundException
     */
    @RequestMapping(value="/get_citizen_id/{citizen_token}", method=RequestMethod.GET)
    public CitizenTokenDTO getCitizenId(@PathVariable String citizen_token) throws UserNotFoundException {
        System.out.println("CitizenToken: " + citizen_token);

        Optional<CitizenToken> optCitizenToken = citizenTokenRepository.findByToken(citizen_token);

        if (optCitizenToken.isPresent()) {
            CitizenToken citizenToken = optCitizenToken.get();

            CitizenTokenDTO citizenTokenDTO = new CitizenTokenDTO();
            citizenTokenDTO.setToken(citizenToken.getToken());
            citizenTokenDTO.setCitizenId(citizenToken.getCitizenId());

            return citizenTokenDTO;
        }
        throw new UserNotFoundException();
    }

    /*
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

    */

}
