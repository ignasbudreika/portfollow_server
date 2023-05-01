package com.github.ignasbudreika.portfollow.external.client;

import com.github.ignasbudreika.portfollow.exception.InvalidExternalRequestException;
import com.github.ignasbudreika.portfollow.external.dto.response.PositionDTO;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class AlpacaClient {
    private static final String ALPACA_POSITIONS_ENDPOINT = "/v2/positions";
    private static final String ALPACA_API_KEY_HEADER = "APCA-API-KEY-ID";
    private static final String ALPACA_SECRET_HEADER = "APCA-API-SECRET-KEY";

    @Value("${http.client.alpaca.base.url}")
    private String baseUrl;

    @Autowired
    private HttpClient client;

    @Autowired
    @Qualifier("wrapped")
    private ObjectMapper wrappedObjectMapper;

    @Autowired
    @Qualifier("unwrapped")
    private ObjectMapper unwrappedObjectMapper;

    public List<PositionDTO> getPositions(String apiKey, String secret) throws IOException, URISyntaxException, InterruptedException, InvalidExternalRequestException {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(new URI(String.format("%s%s", baseUrl, ALPACA_POSITIONS_ENDPOINT)))
                .header(ALPACA_API_KEY_HEADER, apiKey)
                .header(ALPACA_SECRET_HEADER, secret)
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        if (HttpStatus.valueOf(response.statusCode()).is4xxClientError()) {
            throw new InvalidExternalRequestException(String.format("positions request for API key: %s failed with status code: %s and response body: %s",
                    apiKey,
                    response.statusCode(),
                    response.body()));
        }

        return wrappedObjectMapper.readValue(response.body(), new TypeReference<List<PositionDTO>>(){});
    }
}
