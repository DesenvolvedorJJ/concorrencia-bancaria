package br.com.faculdade.concorrencia.exception;

import br.com.faculdade.concorrencia.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * PARTE 2 - O CORACAO DA SOLUCAO.
     *
     * Quando duas requisicoes concorrentes alteram a mesma conta versionada, o
     * Hibernate detecta o conflito de versao e o Spring lanca esta excecao.
     * Em vez de devolver um erro 500 cru, tratamos como 409 CONFLICT, deixando
     * claro para o cliente que a operacao deve ser tentada novamente.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> tratarConflitoDeVersao(ObjectOptimisticLockingFailureException ex,
                                                               HttpServletRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Conflito de concorrencia: a conta foi alterada por outra operacao simultanea. "
                        + "Tente novamente.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarContaNaoEncontrada(ContaNaoEncontradaException ex,
                                                                 HttpServletRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErroResponse> tratarSaldoInsuficiente(SaldoInsuficienteException ex,
                                                                HttpServletRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensagem,
                request.getRequestURI());
        return ResponseEntity.badRequest().body(corpo);
    }
}
