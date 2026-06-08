package br.com.faculdade.concorrencia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

/**
 * PARTE 2 - Conta bancaria COM controle de concorrencia OTIMISTA.
 *
 * O atributo {@link #version} anotado com {@link Version} faz o Hibernate
 * incluir a versao na clausula WHERE de cada UPDATE:
 *
 *     UPDATE conta_bancaria_versionada
 *        SET saldo = ?, version = ?
 *      WHERE id = ? AND version = ?
 *
 * Se duas transacoes leem a mesma versao e ambas tentam gravar, apenas a
 * primeira encontra a linha (version confere). A segunda atualiza 0 linhas e
 * o Hibernate lanca OptimisticLockException, que o Spring traduz em
 * org.springframework.orm.ObjectOptimisticLockingFailureException.
 */
@Entity
@Table(name = "conta_bancaria_versionada")
public class ContaBancariaVersionada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titular;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    // ESTE e o campo que habilita o Lock Otimista.
    @Version
    @Column(nullable = false)
    private Integer version;

    protected ContaBancariaVersionada() {
        // exigido pelo JPA
    }

    public ContaBancariaVersionada(String titular, BigDecimal saldo) {
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

    public Integer getVersion() {
        return version;
    }
}
