package it.unisalento.pas.smartcitywastemanagement.exceptions;

import it.unisalento.pas.smartcitywastemanagement.dto.ExceptionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *  Quando una eccezione viene sollevata, questa classe si occupa di restituire nel body
 *  un json contenente le informazioni sull'eccezione.
 *  Informazioni eccezione:
 *      - codice eccezione (fa riferimento ad un elenco di eccezioni già stilato su un documento excel)
 *      - nome eccezione (nome della Classe Java relativa all'eccezione)
 *      - descrizione eccezione
 */
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PasswordNotMatchingException.class)
    public ResponseEntity<Object> passwordNotMachingHandler(PasswordNotMatchingException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(
                        3,
                        PasswordNotMatchingException.class.getSimpleName(),
                        "Invalid password"
                ));
    }

    @ExceptionHandler(TokenNotMatchingException.class)
    public ResponseEntity<Object> tokenNotMatchingHandler(TokenNotMatchingException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        2,
                        TokenNotMatchingException.class.getSimpleName(),
                        "Invalid Token password reset"
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> userNotFoundHandler(UserNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDTO(
                        1,
                        UserNotFoundException.class.getSimpleName(),
                        "User not found"
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> usernameNotFoundHandler(UsernameNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDTO(
                        4,
                        UsernameNotFoundException.class.getSimpleName(),
                        "Username not found"
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> authenticationExceptionHandler(AuthenticationException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        5,
                        AuthenticationException.class.getSimpleName(),
                        "Invalid credentials"
                ));
    }


    @ExceptionHandler(MailException.class)
    public ResponseEntity<Object> mailExceptionHandler(MailException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        6,
                        MailException.class.getSimpleName(),
                        "Error during email sending"
                ));
    }

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<Object> citizenNotFoundHandler(CitizenNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDTO(
                        7,
                        CitizenNotFoundException.class.getSimpleName(),
                        "Citizen not found"
                ));
    }
}
