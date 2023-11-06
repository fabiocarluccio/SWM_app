package it.unisalento.pas.smartcitywastemanagement.service;

import it.unisalento.pas.smartcitywastemanagement.domain.CitizenToken;
import it.unisalento.pas.smartcitywastemanagement.exceptions.CitizenNotFoundException;
import it.unisalento.pas.smartcitywastemanagement.repositories.CitizenTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitizenTokenServiceImpl implements CitizenTokenService {

    @Autowired
    CitizenTokenRepository citizenTokenRepository;

    public String getCitizenIDByToken(String citizenToken) throws CitizenNotFoundException {

        Optional<CitizenToken> optCitizenToken = citizenTokenRepository.findByToken(citizenToken);

        if(!optCitizenToken.isPresent())
            throw new CitizenNotFoundException();

        return optCitizenToken.get().getCitizenId();
    }

    public String getCitizenTokenByCitizenID(String citizenID) throws CitizenNotFoundException {

        Optional<CitizenToken> optCitizenToken = citizenTokenRepository.findByCitizenId(citizenID);

        if(!optCitizenToken.isPresent())
            throw new CitizenNotFoundException();

        return optCitizenToken.get().getToken();
    }

    public void addCitizenToken(String citizenID) {

        CitizenToken newCitizenToken = new CitizenToken();
        newCitizenToken.setCitizenId(citizenID);

        citizenTokenRepository.save(newCitizenToken);
    }
}
