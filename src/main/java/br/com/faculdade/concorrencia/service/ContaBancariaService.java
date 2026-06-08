package br.com.faculdade.concorrencia.service;

import br.com.faculdade.concorrencia.exception.ContaNaoEncontradaException;
import br.com.faculdade.concorrencia.exception.SaldoInsuficienteException;
import br.com.faculdade.concorrencia.model.ContaBancaria;
import br.com.faculdade.concorrencia.repository.ContaBancariaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * PARTE 1 - Regras de negocio SEM tratamento de concorrencia.
 *
 * Apenas o @Transactional basico e usado. O fluxo "ler saldo -> calcular ->
 * gravar" nao e protegido, entao operacoes simultaneas sobre a mesma conta
 * podem causar Lost Update (atualizacao perdida).
 */
@Service
public class ContaBancariaService {

    private final ContaBancariaRepository repository;
    private final long delayMs;

    public ContaBancariaService(ContaBancariaRepository repository,
                                @Value("${app.delay-ms:0}") long delayMs) {
        this.repository = repository;
        this.delayMs = delayMs;
    }

    @Transactional
    public ContaBancaria depositar(Long id, BigDecimal valor) {
        ContaBancaria conta = buscarEntidade(id);

        // 1) LE o saldo atual para a memoria da JVM
        BigDecimal saldoLido = conta.getSaldo();
        // 2) CALCULA o novo saldo
        BigDecimal novoSaldo = saldoLido.add(valor);
        // 3) Atraso artificial: alarga a janela em que outra thread tambem ja
        //    leu o MESMO saldo antigo -> e onde a atualizacao se perde.
        simularProcessamento();
        // 4) GRAVA de volta (UPDATE acontece no commit da transacao)
        conta.setSaldo(novoSaldo);
        return conta;
    }

    @Transactional
    public ContaBancaria sacar(Long id, BigDecimal valor) {
        ContaBancaria conta = buscarEntidade(id);

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
    public ContaBancaria consultar(Long id) {
        return buscarEntidade(id);
    }

    @Transactional
    public ContaBancaria resetar(Long id, BigDecimal saldo) {
        ContaBancaria conta = buscarEntidade(id);
        conta.setSaldo(saldo);
        return conta;
    }

    private ContaBancaria buscarEntidade(Long id) {
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
