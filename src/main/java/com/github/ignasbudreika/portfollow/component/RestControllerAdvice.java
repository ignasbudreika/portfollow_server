package com.github.ignasbudreika.portfollow.component;

import com.github.ignasbudreika.portfollow.api.dto.response.ApiErrorDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ApiErrorDTO> handleEntityExists(EntityNotFoundException exception) {
        log.info("handling entity not found", exception);
        return ResponseEntity.badRequest().body(ApiErrorDTO.builder().key("entity_not_found").message("entity not found").build());
    }

    @ExceptionHandler({BusinessLogicException.class})
    public ResponseEntity<ApiErrorDTO> handleBusinessLogic(BusinessLogicException exception) {
        log.warn("handling business logic", exception);
        return ResponseEntity.badRequest().body(ApiErrorDTO.builder().key("business_logic").message("invalid request").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleException(Exception exception) {
        log.error("exception occurred", exception);
        return ResponseEntity.internalServerError().body(ApiErrorDTO.builder().key("error_occurred").message("something went wrong").build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) ->{

            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        log.info("invalid request: {}", errors);

        return ResponseEntity.badRequest().body(ApiErrorDTO.builder()
                .key("invalid_request_data")
                .message(errors.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining(", "))).build());
    }
}
