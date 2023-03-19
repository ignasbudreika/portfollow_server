package com.github.ignasbudreika.portfollow.external.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Component
public class EthereumWallet {
    @Value("${web3j.http.service.url}")
    private String serviceUrl;

    // todo consider using builder with scheduled executor
    private final Web3j web3j = Web3j.build(new HttpService(serviceUrl));

    public BigDecimal getWalletBalanceInEther(String address) throws IOException {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        BigInteger weiBalance = balance.getBalance();
        BigDecimal etherBalance = Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);

        return etherBalance;
    }
}
