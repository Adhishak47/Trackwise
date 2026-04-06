package com.expense.tracker.config;

import com.expense.tracker.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponseDTO.FieldErrorDTO> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponseDTO.FieldErrorDTO(
                        fe.getField(),
                        fe.getDefaultMessage()))
                .collect(Collectors.toList());

        logger.warn("Validation failed on {}: {}",
                request.getRequestURI(), summariseFieldErrors(ex.getBindingResult().getFieldErrors()));

        ErrorResponseDTO body = new ErrorResponseDTO();
        body.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.setError("Validation failed");
        body.setMessage(fieldErrors.size() + " field(s) failed validation");
        body.setPath(request.getRequestURI());
        body.setFieldErrors(fieldErrors);

        return ResponseEntity.unprocessableEntity().body(body);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String detail = String.format(
                "Parameter '%s' has invalid value '%s'",
                ex.getName(), ex.getValue());

        logger.warn("Type mismatch on {}: {}", request.getRequestURI(), detail);

        ErrorResponseDTO body = buildError(
                HttpStatus.BAD_REQUEST, "Invalid parameter", detail,
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String errorLabel = (status != null) ? status.getReasonPhrase() : "Error";

        logger.warn("{} on {}: {}",
                ex.getStatusCode().value(), request.getRequestURI(), ex.getReason());

        ErrorResponseDTO body = buildError(
                status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
                errorLabel,
                ex.getReason(),
                request.getRequestURI());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        logger.warn("Access denied on {} for user attempting {}",
                request.getRequestURI(), request.getMethod());

        ErrorResponseDTO body = buildError(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to perform this action",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponseDTO body = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI());

        return ResponseEntity.internalServerError().body(body);
    }


    private ErrorResponseDTO buildError(HttpStatus status,
                                        String error,
                                        String message,
                                        String path) {
        ErrorResponseDTO body = new ErrorResponseDTO();
        body.setStatus(status.value());
        body.setError(error);
        body.setMessage(message);
        body.setPath(path);
        return body;
    }

    private String summariseFieldErrors(List<FieldError> errors) {
        return errors.stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }
}
