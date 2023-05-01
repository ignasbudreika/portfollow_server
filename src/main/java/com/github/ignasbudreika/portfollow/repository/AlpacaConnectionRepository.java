package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.model.AlpacaConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlpacaConnectionRepository extends CrudRepository<AlpacaConnection, String> {
    AlpacaConnection findByUserIdAndStatus(String userId, ConnectionStatus status);
}
