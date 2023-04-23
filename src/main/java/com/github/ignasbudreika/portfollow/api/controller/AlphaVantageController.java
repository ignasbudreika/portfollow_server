package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.AssetDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.external.dto.response.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.external.dto.response.ForexDTO;
import com.github.ignasbudreika.portfollow.external.dto.response.StockDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/alpha-vantage")
public class AlphaVantageController {
    private AlphaVantageClient client;

    @GetMapping("/asset/stock/{ticker}")
    public ResponseEntity<AssetDTO> getStockData(@PathVariable("ticker") String ticker) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        StockDTO stock = client.getStockData(ticker);

        return ResponseEntity.ok().body(
                AssetDTO.builder()
                    .symbol(stock.getSymbol())
                    .price(new BigDecimal(stock.getPrice())).build());
    }

    @GetMapping("/asset/crypto/{code}")
    public ResponseEntity<AssetDTO> getCryptocurrencyData(@PathVariable("code") String code) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        CryptocurrencyDTO crypto = client.getCryptocurrencyData(code);

        return ResponseEntity.ok().body(
                AssetDTO.builder()
                        .symbol(crypto.getCryptocurrencyCode())
                        .price(new BigDecimal(crypto.getExchangeRate())).build());
    }

    @GetMapping("/asset/forex/{code}")
    public ResponseEntity<AssetDTO> getCurrencyData(@PathVariable("code") String code) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        ForexDTO forex = client.getCurrencyData(code);

        return ResponseEntity.ok().body(
                AssetDTO.builder()
                        .symbol(forex.getCurrencyCode())
                        .price(new BigDecimal(forex.getExchangeRate())).build());
    }
}
