package com.github.ignasbudreika.portfollow.component;

import com.github.ignasbudreika.portfollow.api.dto.response.ApiErrorDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestControllerAdvice {
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorDTO> handleUnauthorized(UnauthorizedException exception) {
        log.info("handling unauthorized", exception);
        return ResponseEntity.status(401).body(ApiErrorDTO.builder().key("unauthorized").message("access to this resource requires authorization").build());
    }

    @ExceptionHandler({EntityExistsException.class})
    public ResponseEntity<ApiErrorDTO> handleEntityExists(EntityExistsException exception) {
        log.info("handling entity exists", exception);
        return ResponseEntity.badRequest().body(ApiErrorDTO.builder().key("entity_exists").message("entity already exists").build());
    }

    @ExceptionHandler({BusinessLogicException.class})
    public ResponseEntity<ApiErrorDTO> handleBusinessLogic(BusinessLogicException exception) {
        log.warn("handling business logic", exception);
        return ResponseEntity.badRequest().body(ApiErrorDTO.builder().key("business_logic").message("invalid request").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleException(Exception exception) {
        log.error("exception occurred", exception);
        return ResponseEntity.internalServerError().body(ApiErrorDTO.builder().key("error occurred").message("something went wrong").build());
    }
}
