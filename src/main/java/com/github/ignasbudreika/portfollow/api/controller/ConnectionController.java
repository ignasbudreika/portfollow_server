package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.ConnectionsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.EthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.EthereumWalletService;
import com.github.ignasbudreika.portfollow.service.SpectroCoinService;
import com.github.ignasbudreika.portfollow.service.UserService;
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

    @GetMapping
    public ResponseEntity<ConnectionsDTO> getConnections() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        SpectroCoinConnectionDTO spectro = spectroCoinService.getConnection(user.getId());
        EthereumWalletConnectionDTO ethereumWallet = walletService.getConnectionByUserId(user.getId());

        return ResponseEntity.ok(ConnectionsDTO.builder()
                .spectroCoinConnection(spectro)
                .ethereumWalletConnection(ethereumWallet).build());
    }

    @PostMapping("/spectrocoin")
    public ResponseEntity connectSpectroCoin(@RequestBody com.github.ignasbudreika.portfollow.api.dto.request.SpectroCoinConnectionDTO connectionDTO) throws Exception {
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
    public ResponseEntity connectEthereumWallet(@RequestBody com.github.ignasbudreika.portfollow.api.dto.request.EthereumWalletConnectionDTO walletConnectionDTO) throws Exception {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.addConnection(walletConnectionDTO, user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ethereum/fetch")
    public ResponseEntity fetchWallets() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.fetchBalances(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/ethereum")
    public ResponseEntity removeEthereumWalletConnection() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.removeConnection(user);

        return ResponseEntity.noContent().build();
    }
}
