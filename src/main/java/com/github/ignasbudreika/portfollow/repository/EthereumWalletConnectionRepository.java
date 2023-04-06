package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.EthereumWalletConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EthereumWalletConnectionRepository extends CrudRepository<EthereumWalletConnection, String> {
    EthereumWalletConnection findByUserIdAndAddress(String userId, String address);
    EthereumWalletConnection findByUserId(String userId);

    Iterable<EthereumWalletConnection> findAllByUserId(String userId);
}
