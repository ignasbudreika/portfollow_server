package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.SpectroCoinConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpectroCoinConnectionRepository extends CrudRepository<SpectroCoinConnection, String> {
    SpectroCoinConnection findByUserId(String userId);
}
