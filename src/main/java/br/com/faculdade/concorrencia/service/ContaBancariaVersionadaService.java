package br.com.faculdade.concorrencia.service;

import br.com.faculdade.concorrencia.exception.ContaNaoEncontradaException;
import br.com.faculdade.concorrencia.exception.SaldoInsuficienteException;
import br.com.faculdade.concorrencia.model.ContaBancariaVersionada;
import br.com.faculdade.concorrencia.repository.ContaBancariaVersionadaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * PARTE 2 - Mesmas regras de negocio, mas sobre a entidade @Version.
 *
 * O codigo e quase identico ao da Parte 1. A diferenca esta na ENTIDADE: como
 * {@link ContaBancariaVersionada} possui @Version, o conflito de escrita
 * concorrente faz o commit falhar com ObjectOptimisticLockingFailureException,
 * impedindo a atualizacao perdida.
 */
@Service
public class ContaBancariaVersionadaService {

    private final ContaBancariaVersionadaRepository repository;
    private final long delayMs;

    public ContaBancariaVersionadaService(ContaBancariaVersionadaRepository repository,
                                          @Value("${app.delay-ms:0}") long delayMs) {
        this.repository = repository;
        this.delayMs = delayMs;
    }

    @Transactional
    public ContaBancariaVersionada depositar(Long id, BigDecimal valor) {
        ContaBancariaVersionada conta = buscarEntidade(id);

        BigDecimal novoSaldo = conta.getSaldo().add(valor);
        simularProcessamento();
        conta.setSaldo(novoSaldo);
        return conta;
        // No commit: UPDATE ... WHERE id = ? AND version = ?
        // Se a versao ja mudou -> ObjectOptimisticLockingFailureException -> HTTP 409
    }

    @Transactional
    public ContaBancariaVersionada sacar(Long id, BigDecimal valor) {
        ContaBancariaVersionada conta = buscarEntidade(id);

        BigDecimal saldoLido = conta.getSaldo();
        if (saldoLido.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(id, saldoLido, valor);
        }
        BigDecimal novoSaldo = saldoLido.subtract(valor);
        simularProcessamento();
        conta.setSaldo(novoSaldo);
        return conta;
    }

    @Transactional(readOnly = true)
    public ContaBancariaVersionada consultar(Long id) {
        return buscarEntidade(id);
    }

    @Transactional
    public ContaBancariaVersionada resetar(Long id, BigDecimal saldo) {
        ContaBancariaVersionada conta = buscarEntidade(id);
        conta.setSaldo(saldo);
        return conta;
    }

    private ContaBancariaVersionada buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    private void simularProcessamento() {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
