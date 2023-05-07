package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.SettingsUpdateDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SettingsDTO;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.SettingsService;
import com.github.ignasbudreika.portfollow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingsController {
    @Autowired
    private UserService userService;
    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsDTO> getUserSettings() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(settingsService.getUserSettings(user));
    }

    @PatchMapping
    public ResponseEntity<SettingsDTO> updateUserSettings(@Valid @RequestBody SettingsUpdateDTO settings) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(settingsService.updateUserSettings(settings, user));
    }
}
