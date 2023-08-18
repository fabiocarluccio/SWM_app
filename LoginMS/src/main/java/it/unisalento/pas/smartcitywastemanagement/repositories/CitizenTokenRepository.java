package it.unisalento.pas.smartcitywastemanagement.repositories;

import it.unisalento.pas.smartcitywastemanagement.domain.CitizenToken;
import it.unisalento.pas.smartcitywastemanagement.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CitizenTokenRepository extends MongoRepository<CitizenToken, String> {

    public Optional<CitizenToken> findByToken(String token);

    public Optional<CitizenToken> findByCitizenId(String citizenId);

}