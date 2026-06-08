package br.com.faculdade.concorrencia.exception;

/**
 * Lancada quando o id informado nao corresponde a nenhuma conta. -> HTTP 404
 */
public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(Long id) {
        super("Conta nao encontrada para o id " + id + ".");
    }
}
