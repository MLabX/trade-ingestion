package com.magiccode.tradeingestion.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.JmsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(DealProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleDealProcessingException(
            DealProcessingException ex, 
            HttpServletRequest request) {
        logger.error("Deal processing error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Deal Processing Error",
                ex.getMessage(),
                request.getRequestURI()
            ));
    }
    
    @ExceptionHandler(ConcurrentModificationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleConcurrentModificationException(
            ConcurrentModificationException ex, 
            HttpServletRequest request) {
        logger.error("Concurrent modification error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Concurrent Modification Error",
                ex.getMessage(),
                request.getRequestURI()
            ));
    }
    
    @ExceptionHandler(JmsException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponse> handleJmsException(
            JmsException ex, 
            HttpServletRequest request) {
        logger.error("JMS error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Messaging Service Error",
                "Error communicating with messaging service",
                request.getRequestURI()
            ));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
            
        logger.error("Validation error: {}", errors);
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Invalid input data",
                request.getRequestURI(),
                errors
            ));
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, 
            HttpServletRequest request) {
        logger.error("Uncaught error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
            ));
    }
} 