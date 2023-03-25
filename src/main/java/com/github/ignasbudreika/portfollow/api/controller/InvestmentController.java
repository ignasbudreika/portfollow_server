package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.InvestmentService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/investment")
public class InvestmentController {
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Collection<InvestmentDTO>> getInvestments(@RequestParam(value = "type", required = false) String type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        if (StringUtils.isBlank(type)) {
            return ResponseEntity.ok(investmentService.getUserInvestments(user));
        }

        return ResponseEntity.ok(investmentService.getUserInvestmentsByType(user, InvestmentType.valueOf(type)));
    }
}
