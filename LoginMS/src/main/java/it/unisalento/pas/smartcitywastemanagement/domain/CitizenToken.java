package it.unisalento.pas.smartcitywastemanagement.domain;

import org.springframework.data.annotation.Id;

@Document("citizenToken")
public class CitizenToken {
    @Id
    private String token;
    private String citizenId;


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
