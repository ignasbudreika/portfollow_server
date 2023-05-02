package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private UserRepository repo;

    public boolean existsByGoogleId(String googleId) {
        return repo.existsByGoogleId(googleId);
    }

    public User getByGoogleId(String googleId) {
        return repo.getByGoogleId(googleId);
    }

    public Iterable<User> getAll() { return repo.findAll(); }

    public User createUser(User user) {
        if (repo.existsByEmail(user.getEmail()) || repo.existsByGoogleId(user.getGoogleId())) {
            throw new EntityExistsException(String.format("user with email: %s or googleId: %s already exists", user.getEmail(), user.getGoogleId()));
        }

        return repo.save(user);
    }
}