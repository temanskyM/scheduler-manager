package com.example.exception;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    //    @ExceptionHandler(MethodArgumentNotValidException.class)
    //    @ResponseStatus(HttpStatus.BAD_REQUEST)
    //    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    //        Map<String, String> errors = new HashMap<>();
    //        ex.getBindingResult().getAllErrors().forEach(error -> {
    //            String fieldName = ((FieldError) error).getField();
    //            String errorMessage = error.getDefaultMessage();
    //            errors.put(fieldName, errorMessage);
    //        });
    //        return ResponseEntity.badRequest().body(errors);
    //    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
} 