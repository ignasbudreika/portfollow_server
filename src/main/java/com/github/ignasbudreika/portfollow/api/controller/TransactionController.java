package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.TransactionDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.InvestmentTransactionService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tx")
public class TransactionController {
    @Autowired
    private UserService userService;
    @Autowired
    private InvestmentTransactionService transactionService;

    @PatchMapping("/{id}")
    public ResponseEntity editTransaction(@PathVariable(name = "id") String id, @RequestBody TransactionDTO tx) {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTransaction(@PathVariable(name = "id") String id) throws UnauthorizedException, BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        transactionService.deleteTransaction(id, user);

        return ResponseEntity.noContent().build();
    }
}
