package com.blog.search.config;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return getResponseEntity("Exception", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> HandleRuntimeException(Exception ex) {
        return getResponseEntity("RuntimeException", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> HandleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return getResponseEntity("MethodArgumentNotValidException", ex.getBindingResult().getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JSONException.class)
    public ResponseEntity<Object> HandleJSONException(JSONException ex) {
        return getResponseEntity("JSONException", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> getResponseEntity(String errorType, String errorMessage, HttpStatus status) {
        // 에러 메시지를 포함하는 JSON 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("errorType", errorType);
        response.put("message", errorMessage);

        return new ResponseEntity<>(response, status);
    }

}
