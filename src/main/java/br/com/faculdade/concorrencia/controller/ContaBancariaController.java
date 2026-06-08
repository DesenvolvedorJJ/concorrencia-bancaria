package br.com.faculdade.concorrencia.controller;

import br.com.faculdade.concorrencia.dto.ContaResponse;
import br.com.faculdade.concorrencia.dto.MovimentacaoRequest;
import br.com.faculdade.concorrencia.service.ContaBancariaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * PARTE 1 - Endpoints da conta SEM controle de concorrencia.
 */
@RestController
@RequestMapping("/contas")
public class ContaBancariaController {

    private final ContaBancariaService service;

    public ContaBancariaController(ContaBancariaService service) {
        this.service = service;
    }

    @PostMapping("/{id}/deposito")
    public ContaResponse depositar(@PathVariable Long id,
                                   @RequestBody @Valid MovimentacaoRequest request) {
        return ContaResponse.de(service.depositar(id, request.valor()));
    }

    @PostMapping("/{id}/saque")
    public ContaResponse sacar(@PathVariable Long id,
                               @RequestBody @Valid MovimentacaoRequest request) {
        return ContaResponse.de(service.sacar(id, request.valor()));
    }

    @GetMapping("/{id}")
    public ContaResponse consultar(@PathVariable Long id) {
        return ContaResponse.de(service.consultar(id));
    }

    /** Auxiliar de testes: zera/define o saldo para repetir os cenarios do JMeter. */
    @PostMapping("/{id}/reset")
    public ContaResponse resetar(@PathVariable Long id,
                                 @RequestParam(defaultValue = "0") BigDecimal saldo) {
        return ContaResponse.de(service.resetar(id, saldo));
    }
}
