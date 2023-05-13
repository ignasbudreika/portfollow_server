package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.model.EthereumWalletConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EthereumWalletConnectionRepository extends CrudRepository<EthereumWalletConnection, String> {
    EthereumWalletConnection findByUserIdAndStatus(String userId, ConnectionStatus status);
}
