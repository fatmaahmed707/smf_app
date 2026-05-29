package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.util.AppError;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(AppError.class)
  public ResponseEntity<ApiResponse> handleAppError(AppError e) {
    return ResponseEntity.status(e.getStatus()).body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse> handleAppError(HttpMessageNotReadableException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ApiResponse> handleJwtException(JwtException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<ApiResponse> handleSignatureException(SignatureException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse(false, e.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse(false, message, null));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new ApiResponse(false, "Endpoint not found", null));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse> handleNotFound(NoResourceFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse(false, "Endpoint not found", null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleGeneralError(Exception e) {
    log.error("Unhandled Exception", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse(false, e.getMessage(), null));
  }
}
