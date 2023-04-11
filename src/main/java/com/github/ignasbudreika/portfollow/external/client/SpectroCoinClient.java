package com.github.ignasbudreika.portfollow.external.client;

import com.github.ignasbudreika.portfollow.exception.InvalidExternalRequestException;
import com.github.ignasbudreika.portfollow.external.dto.response.AccessTokenDTO;
import com.github.ignasbudreika.portfollow.external.dto.request.AccessTokenRequestDTO;
import com.github.ignasbudreika.portfollow.external.dto.response.AccountsDTO;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SpectroCoinClient {
    private static final String HTTP_REQUEST_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_REQUEST_HEADER_CONTENT_TYPE_JSON = "application/json";
    private static final String HTTP_REQUEST_HEADER_AUTHORIZATION = "Authorization";

    @Value("${http.client.spectrocoin.oauth.url}")
    private String oauthUrl;
    @Value("${http.client.spectrocoin.oauth.scope}")
    private String oauthScope;
    @Value("${http.client.spectrocoin.oauth.version}")
    private String oauthVersion;
    @Value("${http.client.spectrocoin.wallet.url}")
    private String walletUrl;

    @Autowired
    private HttpClient client;

    @Autowired
    @Qualifier("wrapped")
    private ObjectMapper wrappedObjectMapper;

    @Autowired
    @Qualifier("unwrapped")
    private ObjectMapper unwrappedObjectMapper;

    public AccountsDTO getAccountData(String clientId, String clientSecret) throws IOException, URISyntaxException, InterruptedException, InvalidExternalRequestException {
        String accessToken = getAccessToken(clientId, clientSecret);
        if (accessToken == null) {
            throw new InvalidExternalRequestException(String.format("could not retrieve access token for client: %s", clientId));
        }

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(new URI(walletUrl))
                .header(HTTP_REQUEST_HEADER_AUTHORIZATION, String.format("Bearer %s", accessToken))
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        if (HttpStatus.valueOf(response.statusCode()).is4xxClientError()) {
            throw new InvalidExternalRequestException(String.format("account request for client: %s failed with status code: %s and response body: %s",
                    clientId,
                    response.statusCode(),
                    response.body()));
        }

        return wrappedObjectMapper.readValue(response.body(), AccountsDTO.class);
    }

    public boolean credentialsAreValid(String clientId, String clientSecret) throws IOException, URISyntaxException, InterruptedException {
        return getAccessToken(clientId, clientSecret) != null;
    }

    private String getAccessToken(String clientId, String clientSecret) throws IOException, URISyntaxException, InterruptedException {
        URI uri = UriComponentsBuilder.fromUri(new URI(oauthUrl)).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri)
                .header(HTTP_REQUEST_HEADER_CONTENT_TYPE, HTTP_REQUEST_HEADER_CONTENT_TYPE_JSON)
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        wrappedObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(AccessTokenRequestDTO.builder()
                            .scope(oauthScope)
                            .version(oauthVersion)
                            .clientId(clientId)
                            .clientSecret(clientSecret).build()
                        ), StandardCharsets.UTF_8)
                ).build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        return wrappedObjectMapper.readValue(response.body(), AccessTokenDTO.class).getAccessToken();
    }
}
