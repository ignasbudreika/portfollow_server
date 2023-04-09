package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.model.SpectroCoinConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpectroCoinConnectionRepository extends CrudRepository<SpectroCoinConnection, String> {
    SpectroCoinConnection findByUserIdAndStatus(String userId, ConnectionStatus status);
}
