package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateAlpacaConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.InvalidExternalRequestException;
import com.github.ignasbudreika.portfollow.external.client.AlpacaClient;
import com.github.ignasbudreika.portfollow.external.dto.response.PositionDTO;
import com.github.ignasbudreika.portfollow.model.AlpacaConnection;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.AlpacaConnectionRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlpacaServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String API_KEY = "api_key";
    private static final String SECRET = "secret";
    private static final String CONNECTION_ID = "799ccaeb-758c-46bf-bc43-487847df5554";
    private static final String SYMBOL = "AAPL";
    private static final String ASSET_CLASS = "us_equity";
    private static final BigDecimal QUANTITY = BigDecimal.ONE;

    private final AlpacaConnectionRepository alpacaConnectionRepository = mock(AlpacaConnectionRepository.class);
    private final InvestmentService investmentService = mock(InvestmentService.class);
    private final AlpacaClient alpacaClient = mock(AlpacaClient.class);
    private final AlpacaService target = new AlpacaService(alpacaConnectionRepository, investmentService, alpacaClient);

    @Test
    void shouldThrowEntityExistsException_whenActiveAlpacaConnectionExistsForUser() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        CreateAlpacaConnectionDTO connection = CreateAlpacaConnectionDTO.builder().build();

        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(AlpacaConnection.builder().build());


        assertThrows(EntityExistsException.class, () -> target.addConnection(connection, user));
    }

    @Test
    void shouldCreateConnectionAndFetchBalances() throws Exception {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        CreateAlpacaConnectionDTO connection = CreateAlpacaConnectionDTO.builder()
                .apiKey(API_KEY)
                .secret(SECRET).build();

        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(null)
                .thenReturn(AlpacaConnection.builder()
                        .apiKey(API_KEY)
                        .secret(SECRET)
                        .status(ConnectionStatus.ACTIVE).build());
        when(alpacaClient.getPositions(API_KEY, SECRET)).thenReturn(List.of(PositionDTO.builder()
                        .symbol(SYMBOL)
                        .assetClass(ASSET_CLASS)
                        .quantity(QUANTITY.toString()).build()));


        target.addConnection(connection, user);


        verify(alpacaConnectionRepository, times(2)).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(alpacaClient).getPositions(API_KEY, SECRET);
    }

    @Test
    void shouldGetConnection_whenActiveConnectionExists() {
        when(alpacaConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(
            AlpacaConnection.builder()
                    .apiKey(API_KEY)
                    .status(ConnectionStatus.ACTIVE)
                    .lastFetched(LocalDateTime.now()).build()
        );


        com.github.ignasbudreika.portfollow.api.dto.response.AlpacaConnectionDTO connection = target.getConnection(USER_ID);


        assertEquals(API_KEY, connection.getApiKey());
        assertEquals(ConnectionStatus.ACTIVE, connection.getStatus());
    }

    @Test
    void shouldGetInactiveConnectionStatus_whenActiveConnectionDoesNotExist() {
        when(alpacaConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(null);


        com.github.ignasbudreika.portfollow.api.dto.response.AlpacaConnectionDTO connection = target.getConnection(USER_ID);


        assertEquals(ConnectionStatus.INACTIVE, connection.getStatus());
    }

    @Test
    void shouldRemoveConnection_whenActiveConnectionExists() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(
                AlpacaConnection.builder()
                        .apiKey(API_KEY)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()
        );


        target.removeConnection(user);


        ArgumentCaptor<AlpacaConnection> captor = ArgumentCaptor.forClass(AlpacaConnection.class);
        verify(alpacaConnectionRepository).save(captor.capture());
        assertEquals(API_KEY, captor.getValue().getApiKey());
        assertNull(captor.getValue().getSecret());
        assertEquals(ConnectionStatus.INACTIVE, captor.getValue().getStatus());
    }

    @Test
    void shouldDoNothing_whenActiveConnectionDoesNotExist() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);


        target.removeConnection(user);


        verify(alpacaConnectionRepository, never()).save(any());
    }

    @Test
    void shouldThrowEntityNotFoundException_whenConnectionDoesNotExist() {
        when(alpacaConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(null);


        assertThrows(EntityNotFoundException.class, () -> target.invalidateConnection(CONNECTION_ID));


        verify(alpacaConnectionRepository, never()).save(any());
    }

    @Test
    void shouldInvalidateConnection_whenConnectionExists() {
        when(alpacaConnectionRepository.findById(CONNECTION_ID)).thenReturn(
                Optional.of(AlpacaConnection.builder()
                        .apiKey(API_KEY)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build())
        );


        target.invalidateConnection(CONNECTION_ID);


        ArgumentCaptor<AlpacaConnection> captor = ArgumentCaptor.forClass(AlpacaConnection.class);
        verify(alpacaConnectionRepository).save(captor.capture());
        assertEquals(API_KEY, captor.getValue().getApiKey());
        assertNull(captor.getValue().getSecret());
        assertEquals(ConnectionStatus.INVALID, captor.getValue().getStatus());
    }

    @Test
    void shouldFetchCryptoCurrencies_whenActiveConnectionExists() throws IOException, URISyntaxException, InterruptedException, InvalidExternalRequestException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(AlpacaConnection.builder()
                        .id(CONNECTION_ID)
                        .apiKey(API_KEY)
                        .secret(SECRET)
                        .status(ConnectionStatus.ACTIVE).build());
        when(alpacaClient.getPositions(API_KEY, SECRET)).thenReturn(List.of(PositionDTO.builder()
                .symbol(SYMBOL)
                .assetClass(ASSET_CLASS)
                .quantity(QUANTITY.toString()).build()));


        target.fetchPositions(user);


        verify(alpacaConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(alpacaClient).getPositions(API_KEY, SECRET);
        verify(investmentService).saveInvestmentFetchedFromConnection(any(), eq(CONNECTION_ID));
    }

    @Test
    void shouldNotFetchCryptocurrencies_whenActiveConnectionDoesNotExist() throws InvalidExternalRequestException, IOException, URISyntaxException, InterruptedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);


        target.fetchPositions(user);


        verify(alpacaConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(alpacaClient, never()).getPositions(any(), any());
    }

    @Test
    void shouldInvalidateConnection_whenActiveConnectionExistsButCredentialsAreInvalid() throws InvalidExternalRequestException, IOException, URISyntaxException, InterruptedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(alpacaConnectionRepository.findById(CONNECTION_ID)).thenReturn(
                Optional.of(AlpacaConnection.builder()
                        .id(CONNECTION_ID)
                        .apiKey(API_KEY)
                        .secret(SECRET)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()));
        when(alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(
                AlpacaConnection.builder()
                        .id(CONNECTION_ID)
                        .apiKey(API_KEY)
                        .secret(SECRET)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()
        );
        when(alpacaClient.getPositions(API_KEY, SECRET)).thenThrow(InvalidExternalRequestException.class);


        target.fetchPositions(user);


        verify(alpacaConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(alpacaClient).getPositions(API_KEY, SECRET);

        ArgumentCaptor<AlpacaConnection> captor = ArgumentCaptor.forClass(AlpacaConnection.class);
        verify(alpacaConnectionRepository).save(captor.capture());
        assertEquals(API_KEY, captor.getValue().getApiKey());
        assertNull(captor.getValue().getSecret());
        assertEquals(ConnectionStatus.INVALID, captor.getValue().getStatus());
    }
}
