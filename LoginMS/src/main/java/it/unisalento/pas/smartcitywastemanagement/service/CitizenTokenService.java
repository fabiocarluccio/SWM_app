package it.unisalento.pas.smartcitywastemanagement.service;

import it.unisalento.pas.smartcitywastemanagement.exceptions.CitizenNotFoundException;

public interface CitizenTokenService {

    String getCitizenIDByToken(String citizenToken) throws CitizenNotFoundException;

    String getCitizenTokenByCitizenID(String citizenID) throws CitizenNotFoundException;

    void addCitizenToken(String citizenID);
}
