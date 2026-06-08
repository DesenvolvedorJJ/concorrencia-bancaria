package br.com.faculdade.concorrencia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * PARTE 1 - Conta bancaria SEM nenhum controle de concorrencia.
 *
 * Note que NAO existe atributo @Version. Por isso, duas transacoes que leem o
 * mesmo saldo, calculam o novo valor em memoria e gravam de volta podem
 * sobrescrever uma a outra (problema da "Lost Update" / Atualizacao Perdida).
 */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titular;

    // Atributos monetarios SEMPRE em BigDecimal (precisao 19, 2 casas decimais).
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    protected ContaBancaria() {
        // exigido pelo JPA
    }

    public ContaBancaria(String titular, BigDecimal saldo) {
        this.titular = titular;
        this.saldo = saldo;
    }

    public Long getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
