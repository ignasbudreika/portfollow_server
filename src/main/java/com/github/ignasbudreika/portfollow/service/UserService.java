package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository repo;

    public boolean existsById(String id) {
        return repo.existsById(id);
    }

    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    public User getByEmail(String email) {
        return repo.getByEmail(email);
    }

    public boolean existsByGoogleId(String googleId) {
        return repo.existsByGoogleId(googleId);
    }

    public User getByGoogleId(String googleId) {
        return repo.getByGoogleId(googleId);
    }

    public Iterable<User> getAll() { return repo.findAll(); }

    public void createUser(User user) {
        if (repo.existsByEmail(user.getEmail()) || repo.existsByGoogleId(user.getGoogleId())) {
            return;
        }

        repo.save(user);
    }
}