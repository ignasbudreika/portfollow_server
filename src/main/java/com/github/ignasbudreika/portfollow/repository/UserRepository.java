package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    public boolean existsByEmail(String email);
    public User getByEmail(String email);
    public boolean existsByGoogleId(String googleId);
    public User getByGoogleId(String googleId);
}
