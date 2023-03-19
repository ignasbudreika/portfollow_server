package com.github.ignasbudreika.portfollow.external.client;

import com.github.ignasbudreika.portfollow.external.dto.AccessTokenDTO;
import com.github.ignasbudreika.portfollow.external.dto.AccessTokenRequestDTO;
import com.github.ignasbudreika.portfollow.external.dto.AccountsDTO;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Autowired
    private HttpClient client;

    @Autowired
    @Qualifier("wrapped")
    private ObjectMapper wrappedObjectMapper;

    @Autowired
    @Qualifier("unwrapped")
    private ObjectMapper unwrappedObjectMapper;

    public AccountsDTO getAccountData(String clientId, String clientSecret) throws IOException, URISyntaxException, InterruptedException {
        String accessToken = getAccessToken(clientId, clientSecret);

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(new URI("https://spectrocoin.com/api/r/wallet/accounts"))
                .header("Authorization", String.format("Bearer %s", accessToken))
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        return wrappedObjectMapper.readValue(response.body(), AccountsDTO.class);
    }

    private String getAccessToken(String clientId, String clientSecret) throws IOException, URISyntaxException, InterruptedException {
        URI uri = UriComponentsBuilder.fromUri(new URI("https://spectrocoin.com/api/r/oauth2/auth")).build().toUri();

        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        wrappedObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(AccessTokenRequestDTO.builder()
                            .scope("user_account")
                            .version("1.0")
                            .clientId(clientId)
                            .clientSecret(clientSecret).build()
                        ), StandardCharsets.UTF_8)
                ).build(), HttpResponse.BodyHandlers.ofString());

        return wrappedObjectMapper.readValue(response.body(), AccessTokenDTO.class).getAccessToken();
    }
}
