package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateEthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.EthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.helper.EthereumWalletHelper;
import com.github.ignasbudreika.portfollow.model.EthereumWalletConnection;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.EthereumWalletConnectionRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class EthereumWalletServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String ADDRESS = "0x9d4272C014bec009e381F11daF9518e2A3758124";
    private static final BigDecimal BALANCE = BigDecimal.ONE;
    private static final String CONNECTION_ID = "43a29381-fd45-4fe7-8962-51973ca7ef9b";
    private final EthereumWalletConnectionRepository walletConnectionRepository = mock(EthereumWalletConnectionRepository.class);
    private final InvestmentService investmentService = mock(InvestmentService.class);
    private final EthereumWalletHelper walletHelper = mock(EthereumWalletHelper.class);
    private final EthereumWalletService target = new EthereumWalletService(walletConnectionRepository, investmentService, walletHelper);

    @Test
    void shouldThrowEntityExistsException_whenActiveEthereumWalletConnectionExistsForUser() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        CreateEthereumWalletConnectionDTO connection = CreateEthereumWalletConnectionDTO.builder().build();

        when(walletConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(EthereumWalletConnection.builder().build());


        assertThrows(EntityExistsException.class, () -> target.addConnection(connection, user));
    }

    @Test
    void shouldCreateConnectionAndFetchBalance() throws Exception {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        CreateEthereumWalletConnectionDTO connection = CreateEthereumWalletConnectionDTO.builder()
                .address(ADDRESS).build();

        when(walletConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(null)
                .thenReturn(EthereumWalletConnection.builder()
                        .id(CONNECTION_ID)
                        .address(ADDRESS)
                        .status(ConnectionStatus.ACTIVE).build());
        when(walletHelper.getWalletBalanceInEther(ADDRESS)).thenReturn(BALANCE);


        target.addConnection(connection, user);


        verify(walletConnectionRepository, times(2)).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(walletHelper).getWalletBalanceInEther(ADDRESS);
        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentService).saveInvestmentFetchedFromConnection(captor.capture(), eq(CONNECTION_ID));
        Assertions.assertEquals(BALANCE.setScale(8, RoundingMode.HALF_UP), captor.getValue().getQuantity());
        Assertions.assertEquals(InvestmentUpdateType.ETHEREUM_WALLET, captor.getValue().getUpdateType());
    }

    @Test
    void shouldGetConnection_whenActiveConnectionExists() {
        when(walletConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(
                EthereumWalletConnection.builder()
                        .address(ADDRESS)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()
        );


        EthereumWalletConnectionDTO connection = target.getConnectionByUserId(USER_ID);


        assertEquals(ADDRESS, connection.getAddress());
        assertEquals(ConnectionStatus.ACTIVE, connection.getStatus());
    }

    @Test
    void shouldGetInactiveConnectionStatus_whenActiveConnectionDoesNotExist() {
        when(walletConnectionRepository.findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE)).thenReturn(null);


        EthereumWalletConnectionDTO connection = target.getConnectionByUserId(USER_ID);


        assertEquals(ConnectionStatus.INACTIVE, connection.getStatus());
    }

    @Test
    void shouldRemoveConnection_whenActiveConnectionExists() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(walletConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(
                EthereumWalletConnection.builder()
                        .address(ADDRESS)
                        .status(ConnectionStatus.ACTIVE)
                        .lastFetched(LocalDateTime.now()).build()
        );


        target.removeConnection(user);


        ArgumentCaptor<EthereumWalletConnection> captor = ArgumentCaptor.forClass(EthereumWalletConnection.class);
        verify(walletConnectionRepository).save(captor.capture());
        assertEquals(ADDRESS, captor.getValue().getAddress());
        assertEquals(ConnectionStatus.INACTIVE, captor.getValue().getStatus());
    }

    @Test
    void shouldDoNothing_whenActiveConnectionDoesNotExist() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(walletConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);


        target.removeConnection(user);


        verify(walletConnectionRepository, never()).save(any());
    }

    @Test
    void shouldFetchEthBalance_whenActiveConnectionExists() throws IOException, URISyntaxException, InterruptedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(walletConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE))
                .thenReturn(EthereumWalletConnection.builder()
                        .id(CONNECTION_ID)
                        .address(ADDRESS)
                        .status(ConnectionStatus.ACTIVE).build());
        when(walletHelper.getWalletBalanceInEther(ADDRESS)).thenReturn(BALANCE);


        target.fetchBalance(user);


        verify(walletConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(walletHelper).getWalletBalanceInEther(ADDRESS);
        verify(investmentService).saveInvestmentFetchedFromConnection(any(), eq(CONNECTION_ID));
    }

    @Test
    void shouldNotFetchBalance_whenActiveConnectionDoesNotExist() throws IOException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(walletConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE)).thenReturn(null);


        target.fetchBalance(user);


        verify(walletConnectionRepository).findByUserIdAndStatus(USER_ID, ConnectionStatus.ACTIVE);
        verify(walletHelper, never()).getWalletBalanceInEther(any());
    }
}
