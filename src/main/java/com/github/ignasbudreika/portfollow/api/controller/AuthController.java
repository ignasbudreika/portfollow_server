package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.AccessTokenDTO;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequestMapping("/oauth2")
public class AuthController {
    private static final String GRANT_TYPE = "authorization_code";
    @Value("${spring.security.oauth2.client.registration.google.redirect_uri}")
    private String redirectUri;
    @Autowired
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Autowired
    private GoogleAuthorizationCodeFlow authorizationCodeFlow;
    @Autowired
    private UserService userService;

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccessTokenDTO> retrieveAccessToken(@RequestParam String code,
                                                              @RequestParam("redirect_uri") String clientRedirectUri,
                                                              @RequestParam("grant_type") String grantType) throws GeneralSecurityException, IOException, MissingRequestValueException {
        if (StringUtils.isBlank(code) ||
                StringUtils.isBlank(clientRedirectUri) || !clientRedirectUri.equals(redirectUri) ||
                StringUtils.isBlank(grantType) || !grantType.equals(GRANT_TYPE)) {
            log.warn("missing authorization data");
            throw new MissingRequestValueException("missing authorization data");
        }

        GoogleAuthorizationCodeTokenRequest tokenRequest = authorizationCodeFlow.newTokenRequest(code);
        tokenRequest.setRedirectUri(redirectUri);
        tokenRequest.setGrantType(GRANT_TYPE);
        GoogleTokenResponse tokenResponse = tokenRequest.execute();

        GoogleIdToken verified = googleIdTokenVerifier.verify(tokenResponse.getIdToken());

        if (!userService.existsByGoogleId(verified.getPayload().getSubject())) {
            log.info("creating new user with email: {}", verified.getPayload().getEmail());
            User user = User.builder().email(verified.getPayload().getEmail()).googleId(verified.getPayload().getSubject()).build();

            userService.createUser(user);
        }

        return ResponseEntity.ok().body(AccessTokenDTO.builder()
                .accessToken(tokenResponse.getIdToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresInSeconds()).build());
    }
}
