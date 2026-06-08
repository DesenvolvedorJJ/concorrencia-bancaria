package br.com.faculdade.concorrencia;

import br.com.faculdade.concorrencia.model.ContaBancaria;
import br.com.faculdade.concorrencia.model.ContaBancariaVersionada;
import br.com.faculdade.concorrencia.repository.ContaBancariaRepository;
import br.com.faculdade.concorrencia.repository.ContaBancariaVersionadaRepository;
import br.com.faculdade.concorrencia.service.ContaBancariaService;
import br.com.faculdade.concorrencia.service.ContaBancariaVersionadaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduz, de forma automatizada e determinística, os dois cenários do
 * trabalho. É a mesma "prova" que o JMeter gera, porém verificável por
 * assertivas.
 *
 * Estratégia: N threads leem o saldo "ao mesmo tempo" (liberadas por um
 * CountDownLatch) e, graças ao atraso artificial de 50 ms, todas leem o
 * MESMO valor antes de qualquer commit.
 */
@SpringBootTest(properties = "app.delay-ms=50")
class ConcorrenciaIntegrationTest {

    private static final int THREADS = 20;
    private static final BigDecimal VALOR = new BigDecimal("1.00");

    @Autowired
    private ContaBancariaService servicoSemLock;
    @Autowired
    private ContaBancariaVersionadaService servicoComLock;
    @Autowired
    private ContaBancariaRepository repoSemLock;
    @Autowired
    private ContaBancariaVersionadaRepository repoComLock;

    @Test
    @DisplayName("PARTE 1 (sem @Version): operacoes concorrentes PERDEM atualizacoes")
    void parte1_semBloqueio_perdeAtualizacoes() throws Exception {
        ContaBancaria conta = repoSemLock.save(new ContaBancaria("teste-parte-1", BigDecimal.ZERO));
        Long id = conta.getId();

        executarConcorrente(() -> {
            servicoSemLock.depositar(id, VALOR);
            return null;
        });

        BigDecimal esperado = VALOR.multiply(BigDecimal.valueOf(THREADS)); // 20 depositos de 1,00 = 20,00
        BigDecimal real = repoSemLock.findById(id).orElseThrow().getSaldo();

        System.out.printf("%n[PARTE 1] %d depositos de %s -> esperado=%s | REAL=%s | PERDIDO=%s%n%n",
                THREADS, VALOR, esperado, real, esperado.subtract(real));

        // O saldo final é MENOR que o esperado: atualizações foram perdidas (Lost Update).
        assertThat(real).isLessThan(esperado);
    }

    @Test
    @DisplayName("PARTE 2 (com @Version): conflitos viram 409 e o saldo permanece CONSISTENTE")
    void parte2_comLockOtimista_mantemConsistencia() throws Exception {
        ContaBancariaVersionada conta =
                repoComLock.save(new ContaBancariaVersionada("teste-parte-2", BigDecimal.ZERO));
        Long id = conta.getId();

        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger conflitos = new AtomicInteger();

        executarConcorrente(() -> {
            try {
                servicoComLock.depositar(id, VALOR);
                sucessos.incrementAndGet();
            } catch (ObjectOptimisticLockingFailureException e) {
                // É exatamente o erro que o controller transforma em HTTP 409.
                conflitos.incrementAndGet();
            }
            return null;
        });

        BigDecimal saldoFinal = repoComLock.findById(id).orElseThrow().getSaldo();
        BigDecimal esperadoPelosSucessos = VALOR.multiply(BigDecimal.valueOf(sucessos.get()));

        System.out.printf("%n[PARTE 2] %d tentativas -> sucessos=%d | conflitos(409)=%d | saldo=%s%n%n",
                THREADS, sucessos.get(), conflitos.get(), saldoFinal);

        // Invariante de consistência: o saldo bate EXATAMENTE com os sucessos. Nada se perdeu.
        assertThat(saldoFinal).isEqualByComparingTo(esperadoPelosSucessos);
        // E o lock otimista realmente atuou (houve pelo menos um conflito rejeitado).
        assertThat(conflitos.get()).isGreaterThan(0);
    }

    /** Dispara {@link #THREADS} threads simultaneamente e aguarda todas terminarem. */
    private void executarConcorrente(Callable<Void> tarefa) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(THREADS);
        try {
            for (int i = 0; i < THREADS; i++) {
                pool.submit(() -> {
                    largada.await();           // todas esperam o "tiro de largada"
                    try {
                        tarefa.call();
                    } finally {
                        chegada.countDown();
                    }
                    return null;
                });
            }
            largada.countDown();               // libera todas ao mesmo tempo
            chegada.await(60, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }
}
