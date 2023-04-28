package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateTransactionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.InvestmentService;
import com.github.ignasbudreika.portfollow.service.InvestmentTransactionService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/investment")
public class InvestmentController {
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private UserService userService;
    @Autowired
    private InvestmentTransactionService transactionService;

    @GetMapping
    public ResponseEntity<Collection<InvestmentDTO>> getInvestments(@RequestParam(value = "type", required = false) String type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        if (StringUtils.isBlank(type)) {
            return ResponseEntity.ok(investmentService.getUserInvestments(user));
        }

        return ResponseEntity.ok(investmentService.getUserInvestmentsByType(user, InvestmentType.valueOf(type)));
    }

    @PostMapping("/{id}/tx")
    public ResponseEntity addTransaction(@PathVariable(name = "id") String id, @RequestBody CreateTransactionDTO tx) throws BusinessLogicException, UnauthorizedException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        investmentService.addTransaction(id, tx, user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteInvestment(@PathVariable(name = "id") String id) throws UnauthorizedException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        investmentService.deleteInvestment(id, user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity stopPeriodicInvestments(@PathVariable(name = "id") String id) throws UnauthorizedException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        investmentService.stopPeriodicInvestments(id, user);

        return ResponseEntity.noContent().build();
    }
}
