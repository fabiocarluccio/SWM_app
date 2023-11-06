package it.unisalento.pas.smartcitywastemanagement.service;

import it.unisalento.pas.smartcitywastemanagement.domain.CitizenToken;
import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.dto.LoginDTO;
import it.unisalento.pas.smartcitywastemanagement.dto.PasswordResetDTO;
import it.unisalento.pas.smartcitywastemanagement.exceptions.PasswordNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.TokenNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.UserNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.repositories.UserRepository;
import it.unisalento.pas.smartcitywastemanagement.security.JwtUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static it.unisalento.pas.smartcitywastemanagement.configuration.SecurityConfig.passwordEncoder;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CitizenTokenService citizenTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;


    public User addUser(User newUser) {
        return userRepository.save(newUser);
    }


    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) throws UserNotFoundException {

        Optional<User> optUser = userRepository.findByUsername(username);

        if (!optUser.isPresent())
            throw new UserNotFoundException();

        return optUser.get();
    }

    public String addCitizenUser(String email, String citizenID) throws MailException {

        // 1. Creazione nuovo utente
        User newUser = new User();

        // 2. Setting username
        newUser.setEmail(email);
        newUser.setUsername(email);

        // 3. Setting password
        String password = User.generatePassword();
        newUser.setPassword(passwordEncoder().encode(password));

        // 4. Setting role
        newUser.setRole("Citizen");

        // 5. Salvataggio nel db
        User savedUser = userRepository.save(newUser);

        // 6. Creazione del token associato
        citizenTokenService.addCitizenToken(citizenID);

        // 7. Invio email credenziali
        emailService.sendCredentialsEmail(newUser.getEmail(),newUser.getUsername(), password);

        return savedUser.getId();
    }

    public String createJwtToken(User user) throws UsernameNotFoundException {

        // Chiedo al framework di spring se l'utente con le credenziali loginDTO è autorizzato oppure no
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        // Ora che abbiamo fatto l'autenticazione con il motore di spring, torniamo ora nel dominio della nostra applicazione.
        // quindi carichiamo l'utente attraverso il repository.
        // È buona norma prendersi tutti i dati dell'utente
        // ad esempio mi prendo il campo mail in modo che mando la mail una volta che lui ha eseguito l'accesso (o tentato di farlo)
        // per cui carichiamolo in memoria
        Optional<User> optUser = userRepository.findByUsername(authentication.getName()); // avremmo potuto prendere lo username anche da loginDTO invece da authentication, fa lo stesso visto che lo abbiamo caricato in authentication da loginDTO

        if (!optUser.isPresent()) { // sto controllo lo ha messo "perche non si sa mai"
            throw new UsernameNotFoundException("L'account " + user.getUsername() + "non è esistente.");
        }

        User validUser = optUser.get();

        System.out.println(validUser.getRole());

        // Questo è un oggetto al cui sto dicendo che, da questo momento in poi, se sto richiamando la logica di business,
        // questo utente è autenticato.
        // Attenzione che questa autenticazione vale a livello di thread. Significa che se io poi rifaccio una altra chiamata
        // api dopo, il context è svuotato! Insomma: se dopo arriva una chiamata di un utente diverso, il context deve essere
        // svuotato, altrimenti quell'utente avrebbe il context di un altro utente.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // genero token jwt
        final String jwt = jwtUtilities.generateToken(validUser.getUsername(), validUser.getRole());

        return jwt;
    }


    public void generatePasswordResetToken(String username) throws UserNotFoundException{

        Optional<User> optUser = userRepository.findByUsername(username);

        if (!optUser.isPresent()) {
            throw new UserNotFoundException();
        }

        User user = optUser.get();

        // Genero token per reset password
        String token = User.generatePasswordResetToken();

        // Aggiungo token in db (user)
        user.setPasswordResetToken(passwordEncoder().encode(token));
        userRepository.save(user);

        // Invio token via email
        emailService.sendResetPasswordToken(user.getEmail(),token);
    }

    public void resetPassword(PasswordResetDTO passwordResetDTO) throws UserNotFoundException, TokenNotMatchingException {

        User user = null;

        Optional<User> optUser = userRepository.findByUsername(passwordResetDTO.getUsername());

        if (optUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        user = optUser.get();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // se il token non corrisponde
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

    public void changePassword(PasswordResetDTO passwordResetDTO) throws UserNotFoundException, PasswordNotMatchingException {

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
}
