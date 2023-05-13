package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateAlpacaConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.request.CreateEthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.request.CreateSpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.AlpacaConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.ConnectionsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.EthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.AlpacaService;
import com.github.ignasbudreika.portfollow.service.EthereumWalletService;
import com.github.ignasbudreika.portfollow.service.SpectroCoinService;
import com.github.ignasbudreika.portfollow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connection")
public class ConnectionController {
    @Autowired
    private UserService userService;
    @Autowired
    private SpectroCoinService spectroCoinService;
    @Autowired
    private EthereumWalletService walletService;
    @Autowired
    private AlpacaService alpacaService;

    @GetMapping
    public ResponseEntity<ConnectionsDTO> getConnections() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        SpectroCoinConnectionDTO spectro = spectroCoinService.getConnection(user.getId());
        EthereumWalletConnectionDTO ethereumWallet = walletService.getConnectionByUserId(user.getId());
        AlpacaConnectionDTO alpaca = alpacaService.getConnection(user.getId());

        return ResponseEntity.ok(ConnectionsDTO.builder()
                .spectroCoinConnection(spectro)
                .ethereumWalletConnection(ethereumWallet)
                .alpacaConnection(alpaca).build());
    }

    @PostMapping("/spectrocoin")
    public ResponseEntity connectSpectroCoin(@Valid @RequestBody CreateSpectroCoinConnectionDTO connectionDTO) throws Exception {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        spectroCoinService.addConnection(connectionDTO, user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/spectrocoin/fetch")
    public ResponseEntity fetchSpectrocoin() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        spectroCoinService.fetchCryptocurrencies(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/spectrocoin")
    public ResponseEntity removeSpectrocoinConnection() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        spectroCoinService.removeConnection(user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ethereum")
    public ResponseEntity connectEthereumWallet(@Valid @RequestBody CreateEthereumWalletConnectionDTO walletConnectionDTO) throws Exception {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.addConnection(walletConnectionDTO, user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ethereum/fetch")
    public ResponseEntity fetchWallet() throws BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.fetchBalance(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/ethereum")
    public ResponseEntity removeEthereumWalletConnection() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.removeConnection(user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alpaca")
    public ResponseEntity connectAlpaca(@Valid @RequestBody CreateAlpacaConnectionDTO alpacaConnectionDTO) throws Exception {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        alpacaService.addConnection(alpacaConnectionDTO, user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alpaca/fetch")
    public ResponseEntity fetchPositions() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        alpacaService.fetchPositions(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/alpaca")
    public ResponseEntity removeAlpacaConnection() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        alpacaService.removeConnection(user);

        return ResponseEntity.noContent().build();
    }
}
