package it.unisalento.pas.smartcitywastemanagement.dto;

public class CitizenRegistrationDTO {
    private String citizenId;

    private String email;

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
