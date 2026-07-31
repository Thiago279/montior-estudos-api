package com.toma.monitor_estudos.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.ExceptionTestController.class,
        GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // DTO e Dummy Controller estático
    // -------------------------------------------------------------------------

    record SampleRequest(@NotBlank(message = "não pode ser vazio") String titulo) {}

    @RestController
    @RequestMapping("/teste-excecoes")
    static class ExceptionTestController {

        @GetMapping("/sessao-invalida")
        void throwSessaoInvalida() {
            throw new SessaoInvalidaException("Horário de início posterior ao fim");
        }

        @GetMapping("/sessao-em-andamento")
        void throwSessaoEmAndamento() {
            throw new SessaoEmAndamentoException("Já existe uma sessão ativa para esta matéria");
        }

        @GetMapping("/sessao-ja-finalizada")
        void throwSessaoJaFinalizada() {
            throw new SessaoJaFinalizadaException("A sessão já se encontra encerrada");
        }

        @GetMapping("/entity-not-found")
        void throwEntityNotFound() {
            throw new EntityNotFoundException("Matéria não encontrada com o ID informado");
        }

        @PostMapping("/validacao")
        void throwValidation(@RequestBody @Valid SampleRequest request) {
            // Provoca MethodArgumentNotValidException
        }

        @GetMapping("/periodo-invalido")
        void throwPeriodoInvalido() {
            throw new PeriodoInvalidoException("A data de início deve ser menor que a data final");
        }

        @GetMapping("/parametro-ausente")
        void throwMissingParameter(@RequestParam String inicio) {
            // Provoca MissingServletRequestParameterException se não enviar ?inicio=
        }

        @GetMapping("/erro-inesperado")
        void throwGenericException() {
            throw new RuntimeException("Falha inesperada no banco ou serviço externo");
        }
    }

    // -------------------------------------------------------------------------
    // Testes de Mapeamento dos Handlers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao capturar SessaoInvalidaException")
    void handleSessaoInvalida_DeveRetornar400() throws Exception {
        mockMvc.perform(get("/teste-excecoes/sessao-invalida"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Horário de início posterior ao fim"))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/sessao-invalida"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao capturar SessaoEmAndamentoException")
    void handleSessaoEmAndamento_DeveRetornar409() throws Exception {
        mockMvc.perform(get("/teste-excecoes/sessao-em-andamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Já existe uma sessão ativa para esta matéria"))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/sessao-em-andamento"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao capturar SessaoJaFinalizadaException")
    void handleSessaoJaFinalizada_DeveRetornar400() throws Exception {
        mockMvc.perform(get("/teste-excecoes/sessao-ja-finalizada"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("A sessão já se encontra encerrada"))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/sessao-ja-finalizada"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao capturar EntityNotFoundException")
    void handleEntityNotFoundException_DeveRetornar404() throws Exception {
        mockMvc.perform(get("/teste-excecoes/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Matéria não encontrada com o ID informado"))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/entity-not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao capturar falha de validação Bean Validation (MethodArgumentNotValidException)")
    void handleValidationException_DeveRetornar400EMensagemFormatada() throws Exception {
        mockMvc.perform(post("/teste-excecoes/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("titulo: não pode ser vazio")))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/validacao"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao capturar PeriodoInvalidoException")
    void handlePeriodoInvalidoException_DeveRetornar400() throws Exception {
        mockMvc.perform(get("/teste-excecoes/periodo-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A data de início deve ser menor que a data final"))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/periodo-invalido"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao capturar ausência de parâmetro obrigatório (MissingServletRequestParameterException)")
    void handleMissingServletRequestParameterException_DeveRetornar400() throws Exception {
        mockMvc.perform(get("/teste-excecoes/parametro-ausente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("O parâmetro obrigatório 'inicio' não foi informado"))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/parametro-ausente"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 500 Internal Server Error ao capturar exceções genéricas não tratadas")
    void handleErrosNaoTratados_DeveRetornar500EMensagemGenerica() throws Exception {
        mockMvc.perform(get("/teste-excecoes/erro-inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado no servidor. Por favor, tente novamente mais tarde."))
                .andExpect(jsonPath("$.path").value("/teste-excecoes/erro-inesperado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
