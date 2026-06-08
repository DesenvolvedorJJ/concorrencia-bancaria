package br.com.faculdade.concorrencia.exception;

import java.math.BigDecimal;

/**
 * Lancada quando um saque excede o saldo disponivel. -> HTTP 422
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(Long id, BigDecimal saldoAtual, BigDecimal valorSaque) {
        super("Saldo insuficiente na conta " + id + ". Saldo atual: " + saldoAtual
                + ", valor solicitado: " + valorSaque + ".");
    }
}
