package com.toma.monitor_estudos.exception;

import com.toma.monitor_estudos.dto.erro.ErroResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SessaoInvalidaException.class)
    public ResponseEntity<ErroResponse> handleSessaoInvalida(SessaoInvalidaException ex, HttpServletRequest request){
        ErroResponse erroResponse = buildErroResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroResponse);
    }

    @ExceptionHandler(SessaoEmAndamentoException.class)
    public ResponseEntity<ErroResponse> handleSessaoEmAndamento(SessaoEmAndamentoException ex, HttpServletRequest request){
        ErroResponse erroResponse = buildErroResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erroResponse);
    }

    @ExceptionHandler(SessaoJaFinalizadaException.class)
    public ResponseEntity<ErroResponse> handleSessaoJaFinalizada(SessaoJaFinalizadaException ex, HttpServletRequest request){
        ErroResponse erroResponse = buildErroResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResponse> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        ErroResponse erroResponse = buildErroResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erroResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request){

        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(";\n "));
        ErroResponse erroResponse = buildErroResponse(HttpStatus.BAD_REQUEST,mensagem, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String mensagem = String.format("O parâmetro '%s' recebeu um valor inválido ('%s'). O formato esperado é YYYY-MM-DD.",
                ex.getName(), ex.getValue());

        ErroResponse erro = buildErroResponse(HttpStatus.BAD_REQUEST, mensagem, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler
    public ResponseEntity<ErroResponse> handlePeriodoInvalidoException(PeriodoInvalidoException ex, HttpServletRequest request){
        ErroResponse erro = buildErroResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    private ErroResponse buildErroResponse(HttpStatus status, String mensagem, HttpServletRequest request) {
        return  new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleErrosNaoTratados(Exception ex, HttpServletRequest request) {

        log.error("Erro inesperado", ex);

        ErroResponse erroResponse = buildErroResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor. Por favor, tente novamente mais tarde.",
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erroResponse);
    }

}
