package it.unisalento.pas.smartcitywastemanagement.service;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.dto.PasswordResetDTO;
import it.unisalento.pas.smartcitywastemanagement.exceptions.PasswordNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.TokenNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.UserNotFoundException;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface UserService {

    User addUser(User newUser);

    List<User> getAll();

    User getUserByUsername(String username) throws UserNotFoundException;

    String addCitizenUser(String email, String citizenID) throws MailException;

    String createJwtToken(User user) throws UsernameNotFoundException;

    void generatePasswordResetToken(String username) throws UserNotFoundException;

    void resetPassword(PasswordResetDTO passwordResetDTO) throws UserNotFoundException, TokenNotMatchingException;

    void changePassword(PasswordResetDTO passwordResetDTO) throws UserNotFoundException, PasswordNotMatchingException;
}
