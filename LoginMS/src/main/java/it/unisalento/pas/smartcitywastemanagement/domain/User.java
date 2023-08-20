package it.unisalento.pas.smartcitywastemanagement.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("user")
public class User {
    @Id
    private String id;

    @Indexed(unique=true)
    private String username;
    @Indexed(unique=true)
    private String email;
    private String password;
    private String role;


    private String passwordResetToken;

    static public String generatePassword() {
        int length = 12; // Lunghezza della stringa desiderata
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            char randomChar = characters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }
    static public String generatePasswordResetToken() {
        int length = 6; // Lunghezza della stringa desiderata
        String characters = "0123456789";
        StringBuilder randomString = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            char randomChar = characters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
