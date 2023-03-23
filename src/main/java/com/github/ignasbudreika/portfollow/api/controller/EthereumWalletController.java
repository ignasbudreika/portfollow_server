package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.EthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.EthereumWalletService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/connection/ethereum")
public class EthereumWalletController {
    @Autowired
    private UserService userService;
    @Autowired
    private EthereumWalletService walletService;

    @PostMapping
    public ResponseEntity connectWallet(@RequestBody EthereumWalletConnectionDTO walletConnectionDTO) throws Exception {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.addConnection(walletConnectionDTO, user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fetch")
    public ResponseEntity fetchWallets() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        walletService.fetchBalances(user);

        return ResponseEntity.noContent().build();
    }
}
