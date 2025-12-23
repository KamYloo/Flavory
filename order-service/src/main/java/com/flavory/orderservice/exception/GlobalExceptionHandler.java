package com.flavory.orderservice.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Walidacja nie powiodła się")
                .message("Nieprawidłowe dane wejściowe")
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DishNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleDishNotAvailableException(
            DishNotAvailableException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAddressNotFoundException(
            AddressNotFoundException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
            OrderNotFoundException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatusException(
            InvalidOrderStatusException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedOrderAccessException(
            UnauthorizedOrderAccessException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                "Odmowa dostępu",
                request
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Wystąpił nieoczekiwany błąd",
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Błąd zewnętrznego serwisu: " + ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Nieprawidłowy format JSON requestu",
                request
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private ErrorResponse createErrorResponse(HttpStatus status, String message, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private Integer status;
        private String error;
        private String message;
        private String path;
        private Map<String, String> validationErrors;
    }
}
