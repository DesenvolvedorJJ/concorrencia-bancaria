package br.com.faculdade.concorrencia.config;

import br.com.faculdade.concorrencia.model.ContaBancaria;
import br.com.faculdade.concorrencia.model.ContaBancariaVersionada;
import br.com.faculdade.concorrencia.repository.ContaBancariaRepository;
import br.com.faculdade.concorrencia.repository.ContaBancariaVersionadaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Cria, na inicializacao, uma conta de cada tipo com id = 1 e saldo 0,00.
 * Assim os endpoints e o plano JMeter ja funcionam contra a conta 1.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ContaBancariaRepository contaRepository;
    private final ContaBancariaVersionadaRepository versionadaRepository;

    public DataInitializer(ContaBancariaRepository contaRepository,
                           ContaBancariaVersionadaRepository versionadaRepository) {
        this.contaRepository = contaRepository;
        this.versionadaRepository = versionadaRepository;
    }

    @Override
    public void run(String... args) {
        if (contaRepository.count() == 0) {
            contaRepository.save(new ContaBancaria("Conta Sem Bloqueio (Parte 1)", BigDecimal.ZERO));
        }
        if (versionadaRepository.count() == 0) {
            versionadaRepository.save(
                    new ContaBancariaVersionada("Conta Versionada (Parte 2)", BigDecimal.ZERO));
        }
    }
}
