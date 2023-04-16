package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioListDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioStatisticsDTO;
import com.github.ignasbudreika.portfollow.service.PublicPortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/portfolio")
public class PublicPortfolioController {
    @Autowired
    private PublicPortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<PublicPortfolioListDTO> getPublicPortfolios(@RequestParam(value = "index", defaultValue = "0") int index) {
        return ResponseEntity.ok(portfolioService.getPublicPortfolios(index));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicPortfolioStatisticsDTO> getPublicPortfolioStats(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(portfolioService.getPublicPortfolioStats(id));
    }
}
