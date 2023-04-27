package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.InvalidExternalRequestException;
import com.github.ignasbudreika.portfollow.external.client.SpectroCoinClient;
import com.github.ignasbudreika.portfollow.external.dto.response.AccountsDTO;
import com.github.ignasbudreika.portfollow.model.SpectroCoinConnection;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.SpectroCoinConnectionRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SpectroCoinServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CONNECTION_ID = "799ccaeb-758c-46bf-bc43-487847df5554";
    private static final String CURRENCY_CODE = "ETH";

    private final SpectroCoinConnectionRepository spectroCoinConnectionRepository = mock(SpectroCoinConnectionRepository.class);
    private final InvestmentService investmentService = mock(InvestmentService.class);
    private final SpectroCoinClient spectroCoinClient = mock(SpectroCoinClient.class);
    private final SpectroCoinService target = new SpectroCoinService(spectroCoinConnectionRepository, investmentService, spectroCoinClient);

    @Test
    void shouldThrowEntityExistsException_whenActiveSpectroCoinConnectionExistsForUser() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        SpectroCoinConnectionDTO connection = SpectroCoinConnectionDTO.builder().build();

        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(SpectroCoinConnection.builder().build());


        assertThrows(EntityExistsException.class, () -> target.addConnection(connection, user));
    }

    @Test
    void shouldThrowBusinessLogicException_whenSpectroCoinCredentialsAreInvalid() throws IOException, URISyntaxException, InterruptedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        SpectroCoinConnectionDTO connection = SpectroCoinConnectionDTO.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET).build();

        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);
        when(spectroCoinClient.credentialsAreValid(anyString(), anyString())).thenReturn(false);


        assertThrows(BusinessLogicException.class, () -> target.addConnection(connection, user));
    }

    @Test
    void shouldCreateConnectionAndFetchCryptoCurrencies() throws Exception {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        SpectroCoinConnectionDTO connection = SpectroCoinConnectionDTO.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET).build();

        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(null)
                .thenReturn(SpectroCoinConnection.builder()
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .status(ConnectionStatus.ACTIVE).build());
        when(spectroCoinClient.credentialsAreValid(anyString(), anyString())).thenReturn(true);
        when(spectroCoinClient.getAccountData(CLIENT_ID, CLIENT_SECRET)).thenReturn(AccountsDTO.builder().accounts(
                List.of(AccountsDTO.AccountDTO.builder()
                        .balance(BigDecimal.ONE)
                        .currencyCode(CURRENCY_CODE).build())
                        .toArray(AccountsDTO.AccountDTO[]::new)
        ).build());


        target.addConnection(connection, user);


        verify(spectroCoinConnectionRepository, times(2)).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(spectroCoinClient).getAccountData(CLIENT_ID, CLIENT_SECRET);
    }

    @Test
    void shouldGetConnection_whenActiveConnectionExists() {
        when(spectroCoinConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(
            SpectroCoinConnection.builder()
                    .clientId(CLIENT_ID)
                    .status(ConnectionStatus.ACTIVE)
                    .lastFetched(LocalDateTime.now()).build()
        );


        com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO connection = target.getConnection(USER_ID);


        assertEquals(CLIENT_ID, connection.getClientId());
        assertEquals(ConnectionStatus.ACTIVE, connection.getStatus());
    }

    @Test
    void shouldGetInactiveConnectionStatus_whenActiveConnectionDoesNotExist() {
        when(spectroCoinConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(null);


        com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO connection = target.getConnection(USER_ID);


        assertEquals(ConnectionStatus.INACTIVE, connection.getStatus());
    }

    @Test
    void shouldRemoveConnection_whenActiveConnectionExists() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(
                SpectroCoinConnection.builder()
                        .clientId(CLIENT_ID)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()
        );


        target.removeConnection(user);


        ArgumentCaptor<SpectroCoinConnection> captor = ArgumentCaptor.forClass(SpectroCoinConnection.class);
        verify(spectroCoinConnectionRepository).save(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().getClientId());
        assertNull(captor.getValue().getClientSecret());
        assertEquals(ConnectionStatus.INACTIVE, captor.getValue().getStatus());
    }

    @Test
    void shouldDoNothing_whenActiveConnectionDoesNotExist() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);


        target.removeConnection(user);


        verify(spectroCoinConnectionRepository, never()).save(any());
    }

    @Test
    void shouldThrowEntityNotFoundException_whenConnectionDoesNotExist() {
        when(spectroCoinConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(null);


        assertThrows(EntityNotFoundException.class, () -> target.invalidateConnection(CONNECTION_ID));


        verify(spectroCoinConnectionRepository, never()).save(any());
    }

    @Test
    void shouldInvalidateConnection_whenConnectionExists() {
        when(spectroCoinConnectionRepository.findById(CONNECTION_ID)).thenReturn(
                Optional.of(SpectroCoinConnection.builder()
                        .clientId(CLIENT_ID)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build())
        );


        target.invalidateConnection(CONNECTION_ID);


        ArgumentCaptor<SpectroCoinConnection> captor = ArgumentCaptor.forClass(SpectroCoinConnection.class);
        verify(spectroCoinConnectionRepository).save(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().getClientId());
        assertNull(captor.getValue().getClientSecret());
        assertEquals(ConnectionStatus.INVALID, captor.getValue().getStatus());
    }

    @Test
    void shouldFetchCryptoCurrencies_whenActiveConnectionExists() throws IOException, URISyntaxException, InterruptedException, InvalidExternalRequestException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(SpectroCoinConnection.builder()
                        .id(CONNECTION_ID)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .status(ConnectionStatus.ACTIVE).build());
        when(spectroCoinClient.credentialsAreValid(anyString(), anyString())).thenReturn(true);
        when(spectroCoinClient.getAccountData(CLIENT_ID, CLIENT_SECRET)).thenReturn(AccountsDTO.builder().accounts(
                List.of(AccountsDTO.AccountDTO.builder()
                                .balance(BigDecimal.ONE)
                                .currencyCode(CURRENCY_CODE).build())
                        .toArray(AccountsDTO.AccountDTO[]::new)
        ).build());


        target.fetchCryptocurrencies(user);


        verify(spectroCoinConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(spectroCoinClient).getAccountData(CLIENT_ID, CLIENT_SECRET);
        verify(investmentService).saveInvestmentFetchedFromConnection(any(), eq(CONNECTION_ID));
    }

    @Test
    void shouldNotFetchCryptocurrencies_whenActiveConnectionDoesNotExist() throws InvalidExternalRequestException, IOException, URISyntaxException, InterruptedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);


        target.fetchCryptocurrencies(user);


        verify(spectroCoinConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(spectroCoinClient, never()).getAccountData(any(), any());
    }

    @Test
    void shouldInvalidateConnection_whenActiveConnectionExistsButCredentialsAreInvalid() throws InvalidExternalRequestException, IOException, URISyntaxException, InterruptedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(spectroCoinConnectionRepository.findById(CONNECTION_ID)).thenReturn(
                Optional.of(SpectroCoinConnection.builder()
                        .id(CONNECTION_ID)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()));
        when(spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(
                SpectroCoinConnection.builder()
                        .id(CONNECTION_ID)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()
        );
        when(spectroCoinClient.getAccountData(CLIENT_ID, CLIENT_SECRET)).thenThrow(InvalidExternalRequestException.class);


        target.fetchCryptocurrencies(user);


        verify(spectroCoinConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(spectroCoinClient).getAccountData(CLIENT_ID, CLIENT_SECRET);

        ArgumentCaptor<SpectroCoinConnection> captor = ArgumentCaptor.forClass(SpectroCoinConnection.class);
        verify(spectroCoinConnectionRepository).save(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().getClientId());
        assertNull(captor.getValue().getClientSecret());
        assertEquals(ConnectionStatus.INVALID, captor.getValue().getStatus());
    }
}
