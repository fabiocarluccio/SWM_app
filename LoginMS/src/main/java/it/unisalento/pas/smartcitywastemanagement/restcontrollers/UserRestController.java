package it.unisalento.pas.smartcitywastemanagement.restcontrollers;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.dto.*;
import it.unisalento.pas.smartcitywastemanagement.exceptions.CitizenNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.PasswordNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.TokenNotMatchingException;
import it.unisalento.pas.smartcitywastemanagement.exceptions.UserNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.mappers.UserMapper;
import it.unisalento.pas.smartcitywastemanagement.service.CitizenTokenService;
import it.unisalento.pas.smartcitywastemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/authentication")
public class UserRestController {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CitizenTokenService citizenTokenService;

    /**
     * Endpoint usato dall'Admin per registrare nuovi User.
     * Gli User possono avere uno di 4 ruoli: Citizen, MunicipalOffice, WasteManagementCompany, Admin.
     *
     * @param loginDTO
     * @return new User
     */

    @RequestMapping(value="/registration", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginDTO registration(@RequestBody LoginDTO loginDTO) {

        User newUser = userMapper.fromLoginDTOToNewUser(loginDTO);

        User addedUser = userService.addUser(newUser);

        return userMapper.fromUserToLoginDTO(addedUser);
    }

    /**
     * Endpoint usato dall'Ufficio Comunale per registrare nuovi User con ruolo di "Cittadino".
     * Non è possibile inserire User aventi altri ruoli.
     * Viene aggiunto anche il token del cittadino nel database.
     *
     * @param citizenRegistrationDTO
     * @return new User
     */
    @PreAuthorize("hasRole('ROLE_MICROSERVICE-COMMUNICATION')")
    @RequestMapping(value="/citizen_registration", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String citizenRegistration(@RequestBody CitizenRegistrationDTO citizenRegistrationDTO) throws MailException {

        String createdUserID = userService.addCitizenUser(citizenRegistrationDTO.getEmail(), citizenRegistrationDTO.getCitizenId());
        return createdUserID;
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
    @PreAuthorize("hasRole('ROLE_Citizen')")
    @RequestMapping(value="/password_update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void passwordUpdate(@RequestBody PasswordResetDTO passwordResetDTO) throws PasswordNotMatchingException, UserNotFoundException {
        if (passwordResetDTO.getId() == null) {
            throw new UserNotFoundException();
        }

        userService.changePassword(passwordResetDTO);
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

        userService.resetPassword(passwordResetDTO);
    }

    /**
     * Endpoint usato da qualsiasi user per reimpostare la password.
     * Invia mail contenente il Token che permette di reimpostare la password.
     *
     * @throws UserNotFoundException
     */
    @RequestMapping(value="/password_reset_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void passwordResetToken(@RequestBody LoginDTO loginCredentials) throws UserNotFoundException {

        userService.generatePasswordResetToken(loginCredentials.getUsername());
    }

    /**
     * Endpoint di autenticazione.
     * Restituisce token JWT a client in modo che non debba inviare ripetutamente username e password ad ogni richiesta.
     *
     * @param loginDTO
     * @return Token JWT
     */
    @RequestMapping(value="/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) throws UsernameNotFoundException{

        User user = userMapper.fromLoginDTOToUser(loginDTO);
        String jwt = userService.createJwtToken(user);

        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));
    }


    @PreAuthorize("hasAnyRole('ROLE_Admin','ROLE_WasteManagementCompany','ROLE_MunicipalOffice','ROLE_Citizen')")
    @RequestMapping(value="/get_info/{username}", method=RequestMethod.GET)
    public LoginDTO getInfo(@PathVariable String username) throws UserNotFoundException {

        User requestedUser = userService.getUserByUsername(username);

        return userMapper.fromUserToLoginDTO(requestedUser);
    }

    @PreAuthorize("hasRole('ROLE_Citizen')")
    @RequestMapping(value="/getCitizenToken/{citizenID}")
    public ResponseEntity<ResponseDTO> getCitizenToken(@PathVariable("citizenID") String citizenID) throws CitizenNotFoundException {

        String citizenToken = citizenTokenService.getCitizenTokenByCitizenID(citizenID);

        return new ResponseEntity<>(
                new ResponseDTO("success",citizenToken),
                HttpStatus.OK
        );
    }


    @PreAuthorize("hasRole('ROLE_Admin')")
    @RequestMapping(value="/getall", method= RequestMethod.GET)
    public List<LoginDTO> getAll() {

        List<User> allUsers = userService.getAll();

        List<LoginDTO> usersDTO = new ArrayList<>();

        for(User user : allUsers) {
            LoginDTO userDTO = new LoginDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setPassword(null);
            userDTO.setRole(user.getRole());

            usersDTO.add(userDTO);
        }

        return usersDTO;
    }

    /**
     * Endpoint usato per ricavare l'id del cittadino, dato il token.
     * Utilizzato per autorizzare i conferimenti.
     *
     * @param citizen_token
     * @return
     * @throws UserNotFoundException
     */
    @PreAuthorize("hasRole('ROLE_Citizen')")
    @RequestMapping(value="/get_citizen_id/{citizen_token}", method=RequestMethod.GET)
    public String getCitizenId(@PathVariable String citizen_token) throws CitizenNotFoundException {

        return citizenTokenService.getCitizenIDByToken(citizen_token);
    }

}
