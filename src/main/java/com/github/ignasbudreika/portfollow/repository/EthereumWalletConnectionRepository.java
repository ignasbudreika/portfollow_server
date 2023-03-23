package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.EthereumWalletConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EthereumWalletConnectionRepository extends CrudRepository<EthereumWalletConnection, String> {
    EthereumWalletConnection findByAddress(String address);

    Iterable<EthereumWalletConnection> findAllByUserId(String userId);
}
