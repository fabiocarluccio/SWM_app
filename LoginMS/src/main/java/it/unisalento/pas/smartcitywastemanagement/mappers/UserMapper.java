package it.unisalento.pas.smartcitywastemanagement.mappers;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import it.unisalento.pas.smartcitywastemanagement.dto.LoginDTO;
import org.springframework.stereotype.Component;

import static it.unisalento.pas.smartcitywastemanagement.configuration.SecurityConfig.passwordEncoder;

@Component
public class UserMapper {

    public User fromLoginDTOToNewUser(LoginDTO loginDTO) {

        User newUser = new User();

        newUser.setEmail(loginDTO.getEmail());
        newUser.setUsername(loginDTO.getUsername());
        newUser.setPassword(passwordEncoder().encode(loginDTO.getPassword()));
        newUser.setRole(loginDTO.getRole());
        return newUser;
    }

    public User fromLoginDTOToUser(LoginDTO loginDTO) {

        User newUser = new User();

        newUser.setEmail(loginDTO.getEmail());
        newUser.setUsername(loginDTO.getUsername());
        newUser.setPassword(loginDTO.getPassword());
        newUser.setRole(loginDTO.getRole());
        return newUser;
    }

    public LoginDTO fromUserToLoginDTO(User user) {

        LoginDTO loginDTO = new LoginDTO();

        loginDTO.setUsername(user.getUsername());
        loginDTO.setEmail(user.getEmail());
        loginDTO.setRole(user.getRole());
        loginDTO.setId(user.getId());
        loginDTO.setPassword(null);

        return loginDTO;
    }
}
