package com.github.ignasbudreika.portfollow.external.client;

import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.dto.response.*;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private static final String QUERY_PARAM_TIME_SERIES_DAILY = "TIME_SERIES_DAILY";
    // todo use this TIME_SERIES_DAILY_ADJUSTED
    private static final String QUERY_PARAM_FUNCTION_FX_DAILY = "FX_DAILY";
    private static final String QUERY_PARAM_FUNCTION_DIGITAL_CURRENCY_DAILY = "DIGITAL_CURRENCY_DAILY";
    private static final String QUERY_PARAM_CURRENCY_FROM = "from_currency";
    private static final String QUERY_PARAM_CURRENCY_TO = "to_currency";
    private static final String QUERY_PARAM_SYMBOL_FROM = "from_symbol";
    private static final String QUERY_PARAM_SYMBOL_TO = "to_symbol";
    // todo add all prices conversion to eur
    private static final String QUERY_PARAM_CURRENCY_TO_EUR = "EUR";
    private static final String QUERY_PARAM_CURRENCY_TO_USD = "USD";
    private static final String QUERY_PARAM_SYMBOL_TO_EUR = "EUR";
    private static final String QUERY_PARAM_API_KEY = "apikey";
    private static final String QUERY_PARAM_MARKET = "market";
    private static final String QUERY_PARAM_TICKER = "symbol";
    private static final String QUERY_PARAM_OUTPUT_SIZE = "outputsize";
    private static final String QUERY_PARAM_OUTPUT_SIZE_FULL = "full";

    @Value("${http.client.alpha.vantage.base.url}")
    private String baseUrl;

    @Value("${http.client.alpha.vantage.api.key}")
    private String apiKey;

    @Autowired
    private HttpClient client;

    @Autowired
    @Qualifier("unwrapped")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("wrapped")
    private ObjectMapper wrappedObjectMapper;

    public StockDTO getStockData(String ticker) throws IOException, InterruptedException, URISyntaxException, BusinessLogicException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_GLOBAL_QUOTE)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_TICKER, ticker).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        validateResponseBody(response.body());

        return objectMapper.readValue(response.body(), StockDTO.class);
    }

    public CryptocurrencyDTO getCryptocurrencyData(String code) throws IOException, InterruptedException, URISyntaxException, BusinessLogicException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_CURRENCY_EXCHANGE_RATE)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_CURRENCY_FROM, code)
                .queryParam(QUERY_PARAM_CURRENCY_TO, QUERY_PARAM_CURRENCY_TO_USD).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        validateResponseBody(response.body());

        return objectMapper.readValue(response.body(), CryptocurrencyDTO.class);
    }

    public ForexDTO getCurrencyData(String code) throws IOException, InterruptedException, URISyntaxException, BusinessLogicException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_CURRENCY_EXCHANGE_RATE)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_CURRENCY_FROM, code)
                .queryParam(QUERY_PARAM_CURRENCY_TO, QUERY_PARAM_CURRENCY_TO_USD).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        validateResponseBody(response.body());

        return objectMapper.readValue(response.body(), ForexDTO.class);
    }

    public StockHistoryDailyDTO getStockHistoryDaily(String symbol) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_TIME_SERIES_DAILY)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_OUTPUT_SIZE, QUERY_PARAM_OUTPUT_SIZE_FULL)
                .queryParam(QUERY_PARAM_TICKER, symbol).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        validateResponseBody(response.body());

        return wrappedObjectMapper.readValue(response.body(), StockHistoryDailyDTO.class);
    }

    public ForexHistoryDailyDTO getForexHistoryDaily(String currency) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_FX_DAILY)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_OUTPUT_SIZE, QUERY_PARAM_OUTPUT_SIZE_FULL)
                .queryParam(QUERY_PARAM_SYMBOL_FROM, currency)
                .queryParam(QUERY_PARAM_SYMBOL_TO, QUERY_PARAM_CURRENCY_TO_USD).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        validateResponseBody(response.body());

        return wrappedObjectMapper.readValue(response.body(), ForexHistoryDailyDTO.class);
    }

    public CryptocurrencyHistoryDailyDTO getCryptoHistoryDaily(String currency) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        URI uri = UriComponentsBuilder.fromUri(new URI(baseUrl))
                .queryParam(QUERY_PARAM_FUNCTION, QUERY_PARAM_FUNCTION_DIGITAL_CURRENCY_DAILY)
                .queryParam(QUERY_PARAM_API_KEY, apiKey)
                .queryParam(QUERY_PARAM_OUTPUT_SIZE, QUERY_PARAM_OUTPUT_SIZE_FULL)
                .queryParam(QUERY_PARAM_MARKET, QUERY_PARAM_CURRENCY_TO_USD)
                .queryParam(QUERY_PARAM_TICKER, currency).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

        validateResponseBody(response.body());

        return wrappedObjectMapper.readValue(response.body(), CryptocurrencyHistoryDailyDTO.class);
    }

    private void validateResponseBody(String body) throws IOException, BusinessLogicException {
        try {
            ErrorDTO error = objectMapper.readValue(body, ErrorDTO.class);
            if (StringUtils.isNotBlank(error.getMessage())) {
                throw new BusinessLogicException(String.format("failed to get symbol data with error: %s", error.getMessage()));
            }
        } catch (JsonMappingException ignored) {
        }
    }
}
