package it.unisalento.pas.smartcitywastemanagement.repositories;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    public Optional<User> findByEmail(String email);

    public Optional<User> findByUsername(String username);

    List<User> findAll();

    Optional<User> findById(String userID);


}
