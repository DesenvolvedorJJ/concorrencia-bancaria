package br.com.faculdade.concorrencia.dto;

import java.time.OffsetDateTime;

/**
 * Corpo padronizado de erro retornado pelo GlobalExceptionHandler.
 */
public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErroResponse de(int status, String error, String message, String path) {
        return new ErroResponse(OffsetDateTime.now(), status, error, message, path);
    }
}
