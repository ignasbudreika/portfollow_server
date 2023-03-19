package com.github.ignasbudreika.portfollow.component;

import com.github.ignasbudreika.portfollow.api.dto.response.ApiErrorDTO;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestControllerAdvice {
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorDTO> handleUnauthorized(UnauthorizedException exception) {
        log.info("handling unauthorized: {}", exception.getMessage());
        return ResponseEntity.status(401).body(ApiErrorDTO.builder().key("unauthorized").message("access to this resource requires authorization").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleException(Exception exception) {
        log.error("exception occurred: {}", exception.getMessage());
        return ResponseEntity.internalServerError().body(ApiErrorDTO.builder().key("error occurred").message("something went wrong").build());
    }
}
