package com.example.obzharka.exception;

import com.example.obzharka.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> onValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.putIfAbsent(toSnake(e.getField()), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse(errors));
    }

    @ExceptionHandler(InvalidPhoneException.class)
    public ResponseEntity<ErrorResponse> onPhone(InvalidPhoneException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("phone", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> onUnreadable(HttpMessageNotReadableException ex) {
        log.debug("Не удалось разобрать тело запроса", ex);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("request", "Не удалось разобрать заявку, проверьте данные"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> onUnexpected(Exception ex) {
        log.error("Не удалось сохранить заявку", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("server", "Не удалось сохранить заявку, попробуйте ещё раз"));
    }

    private static String toSnake(String field) {
        return field.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}
