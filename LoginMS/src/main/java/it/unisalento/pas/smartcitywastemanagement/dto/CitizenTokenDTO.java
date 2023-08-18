package it.unisalento.pas.smartcitywastemanagement.dto;

public class CitizenTokenDTO {

    private String citizenId;
    private String token;

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
