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
    public ResponseEntity<Object> handleSpecificException(PasswordNotMatchingException ex) {
        // Creare un oggetto di risposta personalizzato per l'eccezione specifica
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(
                        3,
                        PasswordNotMatchingException.class.getSimpleName(),
                        "Invalid password"
                ));
    }

    @ExceptionHandler(TokenNotMatchingException.class)
    public ResponseEntity<Object> handleAnotherException(TokenNotMatchingException ex) {
        // Creare un oggetto di risposta personalizzato per un'altra eccezione specifica
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        2,
                        TokenNotMatchingException.class.getSimpleName(),
                        "Invalid Token password reset"
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleAnotherException(UserNotFoundException ex) {
        // Creare un oggetto di risposta personalizzato per un'altra eccezione specifica
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        1,
                        UserNotFoundException.class.getSimpleName(),
                        "User not found"
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleAnotherException(UsernameNotFoundException ex) {
        // Creare un oggetto di risposta personalizzato per un'altra eccezione specifica
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        4,
                        UsernameNotFoundException.class.getSimpleName(),
                        "Username not found"
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAnotherException(AuthenticationException ex) {
        // Creare un oggetto di risposta personalizzato per un'altra eccezione specifica
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        5,
                        AuthenticationException.class.getSimpleName(),
                        "Invalid credentials"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        // Creare un oggetto di risposta personalizzato per tutte le altre eccezioni
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        0,
                        Exception.class.getSimpleName(),
                        "Internal server error"
                ));
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<Object> handleGenericException(MailException ex) {
        // Creare un oggetto di risposta personalizzato per tutte le altre eccezioni
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(
                        6,
                        MailException.class.getSimpleName(),
                        "Error during email sending"
                ));
    }
}
