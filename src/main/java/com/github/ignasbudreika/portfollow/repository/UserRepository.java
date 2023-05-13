package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByGoogleId(String googleId);
    User getByGoogleId(String googleId);
}
