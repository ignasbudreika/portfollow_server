package com.github.ignasbudreika.portfollow.external.client;

import com.github.ignasbudreika.portfollow.external.dto.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.external.dto.ForexDTO;
import com.github.ignasbudreika.portfollow.external.dto.StockDTO;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class AlphaVantageClient {
    private static final String QUERY_PARAM_FUNCTION = "function";
    private static final String QUERY_PARAM_FUNCTION_GLOBAL_QUOTE = "GLOBAL_QUOTE";
    private static final String QUERY_PARAM_FUNCTION_CURRENCY_EXCHANGE_RATE = "CURRENCY_EXCHANGE_RATE";
    private static final String QUERY_PARAM_CURRENCY_FROM = "from_currency";
    private static final String QUERY_PARAM_CURRENCY_TO = "to_currency";
    private static final String QUERY_PARAM_CURRENCY_TO_EUR = "EUR";
    private static final String QUERY_PARAM_API_KEY = "apikey";
    private static final String QUERY_PARAM_TICKER = "symbol";

    @Value("${http.client.alpha.vantage.base.url}")
    private String baseUrl;

    @Value("${http.client.alpha.vantage.api.key}")
    private String apiKey;

    @Autowired
    private HttpClient client;

    @Autowired
    private ObjectMapper objectMapper;

    public StockDTO getStockData(String ticker) throws IOException, InterruptedException, URISyntaxException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_GLOBAL_QUOTE)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_TICKER, ticker).build().toUri();

        // todo use async requests
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), StockDTO.class);
    }

    public CryptocurrencyDTO getCryptocurrencyData(String code) throws IOException, InterruptedException, URISyntaxException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_CURRENCY_EXCHANGE_RATE)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_CURRENCY_FROM, code)
                .queryParam(QUERY_PARAM_CURRENCY_TO, QUERY_PARAM_CURRENCY_TO_EUR).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), CryptocurrencyDTO.class);
    }

    public ForexDTO getCurrencyData(String code) throws IOException, InterruptedException, URISyntaxException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_CURRENCY_EXCHANGE_RATE)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_CURRENCY_FROM, code)
                .queryParam(QUERY_PARAM_CURRENCY_TO, QUERY_PARAM_CURRENCY_TO_EUR).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), ForexDTO.class);
    }
}
