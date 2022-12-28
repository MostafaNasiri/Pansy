package io.github.mostafanasiri.pansy.features.user;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public User createUser(User user) {
        if (repository.findByUsername(user.getUsername()) != null) {
            throw new InvalidInputException("Username already exists");
        }

        // TODO hash user's password

        return repository.save(user);
    }

    public User updateUser(User user) {
        return repository.save(user);
    }

    public User getUser(int userId) {
        return repository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(User.class, userId)
                );
    }
}
