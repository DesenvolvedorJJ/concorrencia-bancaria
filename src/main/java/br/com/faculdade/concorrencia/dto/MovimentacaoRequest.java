package br.com.faculdade.concorrencia.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Corpo das requisicoes de deposito e saque. Ex.: { "valor": 10.00 }
 */
public record MovimentacaoRequest(

        @NotNull(message = "O campo 'valor' e obrigatorio.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        BigDecimal valor

) {
}
