package it.unisalento.pas.smartcitywastemanagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PasswordNotMatchingException extends Exception {
    /*
    public PasswordNotMatchingException(String message) {
        super(message);
    }
    */
}
