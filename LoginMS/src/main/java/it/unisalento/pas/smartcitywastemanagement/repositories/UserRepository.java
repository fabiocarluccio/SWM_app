package it.unisalento.pas.smartcitywastemanagement.repositories;

import it.unisalento.pas.smartcitywastemanagement.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    public List<User> findBySurname(String surname);

    public User findByUsername(String username);
}
