package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.SpectroCoinService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connection/spectrocoin")
public class SpectroCoinController {
    @Autowired
    private UserService userService;
    @Autowired
    private SpectroCoinService spectroCoinService;

    @PostMapping
    public ResponseEntity addConnection(@RequestBody SpectroCoinConnectionDTO connectionDTO) throws Exception {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        spectroCoinService.addConnection(connectionDTO, user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO> getActiveConnection() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok().body(spectroCoinService.getConnection(user.getId()));
    }

    @PostMapping("/fetch")
    public ResponseEntity fetch() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        spectroCoinService.fetchCryptocurrencies(user);

        return ResponseEntity.noContent().build();
    }
}
