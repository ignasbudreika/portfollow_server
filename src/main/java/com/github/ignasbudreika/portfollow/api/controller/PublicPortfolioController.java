package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.CommentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.AuthorCommentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioListDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioStatisticsDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.PublicPortfolioService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/portfolio")
public class PublicPortfolioController {
    @Autowired
    private PublicPortfolioService portfolioService;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<PublicPortfolioListDTO> getPublicPortfolios(@RequestParam(value = "index", defaultValue = "0") int index) {
        return ResponseEntity.ok(portfolioService.getPublicPortfolios(index));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicPortfolioStatisticsDTO> getPublicPortfolioStats(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(portfolioService.getPublicPortfolioStats(id));
    }

    @GetMapping("/{id}/distribution")
    public ResponseEntity<PublicPortfolioDistributionDTO> getPublicPortfolioDistribution(
            @PathVariable(value = "id") String id,
            @RequestParam(value = "type", required = false) String type) {
        return ResponseEntity.ok(portfolioService.getPublicPortfolioDistribution(id, type));
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity comment(@PathVariable(value = "id") String id, @RequestBody CommentDTO commentDTO) throws BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        portfolioService.comment(user, id, commentDTO);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comment/{id}")
    public ResponseEntity deleteComment(@PathVariable(value = "id") String id) throws BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        portfolioService.deleteComment(user, id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comment")
    public ResponseEntity<AuthorCommentDTO[]> getComments(@PathVariable(value = "id") String id) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(portfolioService.getPortfolioComments(user, id));
    }
}
