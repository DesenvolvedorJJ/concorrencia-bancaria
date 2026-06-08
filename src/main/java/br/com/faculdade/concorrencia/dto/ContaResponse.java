package br.com.faculdade.concorrencia.dto;

import br.com.faculdade.concorrencia.model.ContaBancaria;
import br.com.faculdade.concorrencia.model.ContaBancariaVersionada;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Resposta padrao das operacoes sobre a conta.
 * O campo {@code version} so aparece para a conta versionada (Parte 2);
 * para a conta da Parte 1 ele e nulo e e omitido do JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContaResponse(
        Long id,
        String titular,
        BigDecimal saldo,
        Integer version
) {

    public static ContaResponse de(ContaBancaria conta) {
        return new ContaResponse(conta.getId(), conta.getTitular(), conta.getSaldo(), null);
    }

    public static ContaResponse de(ContaBancariaVersionada conta) {
        return new ContaResponse(conta.getId(), conta.getTitular(), conta.getSaldo(), conta.getVersion());
    }
}
