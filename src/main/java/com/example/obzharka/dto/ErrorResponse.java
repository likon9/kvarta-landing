package com.example.obzharka.dto;

import java.util.Map;

public record ErrorResponse(Map<String, String> errors) {

    public static ErrorResponse of(String field, String message) {
        return new ErrorResponse(Map.of(field, message));
    }
}
