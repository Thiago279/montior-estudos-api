package com.toma.monitor_estudos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toma.monitor_estudos.dto.SessaoEstudoRequest;
import com.toma.monitor_estudos.dto.SessaoEstudoResponse;
import com.toma.monitor_estudos.exception.SessaoJaFinalizadaException;
import com.toma.monitor_estudos.service.SessaoEstudoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessaoEstudoController.class)
class SessaoEstudoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessaoEstudoService sessaoEstudoService;

    // Suporte a serialização de LocalDateTime
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // -------------------------------------------------------------------------
    // Constantes e Helpers
    // -------------------------------------------------------------------------

    private static final String BASE_URL = "/monitor-estudos/sessoes";
    private static final Long ID_SESSAO = 10L;
    private static final Long ID_SESSAO_INEXISTENTE = 99L;
    private static final Long ID_MATERIA = 1L;
    private static final String TITULO_MATERIA = "Java Backend";

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 7, 30, 14, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 7, 30, 16, 0);

    private SessaoEstudoRequest requestPadrao() {
        return new SessaoEstudoRequest(ID_MATERIA, INICIO, FIM);
    }

    private SessaoEstudoResponse responsePadrao() {
        return new SessaoEstudoResponse(ID_SESSAO, INICIO, FIM, ID_MATERIA, TITULO_MATERIA);
    }

    // -------------------------------------------------------------------------
    // POST /monitor-estudos/sessoes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 201 Created ao registrar sessão com dados válidos")
    void criarSessaoSuccess() throws Exception {
        // Arrange
        SessaoEstudoRequest request = requestPadrao();
        SessaoEstudoResponse response = responsePadrao();

        when(sessaoEstudoService.salvar(any(SessaoEstudoRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_SESSAO))
                .andExpect(jsonPath("$.materiaId").value(ID_MATERIA))
                .andExpect(jsonPath("$.materiaTitulo").value(TITULO_MATERIA));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar criar sessão para matéria inexistente")
    void criarSessaoMateriaNotFound() throws Exception {
        // Arrange
        SessaoEstudoRequest request = requestPadrao();

        when(sessaoEstudoService.salvar(any(SessaoEstudoRequest.class)))
                .thenThrow(new EntityNotFoundException("Matéria não encontrada"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar Sessão com dados em branco")
    void criarSessaoInvalid() throws Exception {
        // Arrange
        SessaoEstudoRequest invalidRequest = new SessaoEstudoRequest(null,null,null);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /monitor-estudos/sessoes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK e a lista com o histórico de sessões")
    void listarSuccess() throws Exception {
        // Arrange
        SessaoEstudoResponse response = responsePadrao();
        when(sessaoEstudoService.listarTodas()).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ID_SESSAO))
                .andExpect(jsonPath("$[0].materiaId").value(ID_MATERIA));
    }

    // -------------------------------------------------------------------------
    // DELETE /monitor-estudos/sessoes/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 204 No Content ao deletar sessão com ID existente")
    void deletarSuccess() throws Exception {
        // Arrange
        doNothing().when(sessaoEstudoService).deletar(ID_SESSAO);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", ID_SESSAO))
                .andExpect(status().isNoContent());

        verify(sessaoEstudoService, times(1)).deletar(ID_SESSAO);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar deletar sessão inexistente")
    void deletarIdNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Sessão não encontrada"))
                .when(sessaoEstudoService).deletar(ID_SESSAO_INEXISTENTE);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", ID_SESSAO_INEXISTENTE))
                .andExpect(status().isNotFound());

        verify(sessaoEstudoService, times(1)).deletar(ID_SESSAO_INEXISTENTE);
    }

    // -------------------------------------------------------------------------
    // PUT /monitor-estudos/sessoes/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK ao atualizar sessão existente com dados válidos")
    void atualizarSuccess() throws Exception {
        // Arrange
        SessaoEstudoRequest request = requestPadrao();
        SessaoEstudoResponse response = responsePadrao();

        when(sessaoEstudoService.atualizar(eq(ID_SESSAO), any(SessaoEstudoRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", ID_SESSAO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_SESSAO))
                .andExpect(jsonPath("$.materiaId").value(ID_MATERIA));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar atualizar sessão inexistente")
    void atualizarNotFound() throws Exception {
        // Arrange
        SessaoEstudoRequest request = requestPadrao();

        when(sessaoEstudoService.atualizar(eq(ID_SESSAO_INEXISTENTE), any(SessaoEstudoRequest.class)))
                .thenThrow(new EntityNotFoundException("Sessão não encontrada"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", ID_SESSAO_INEXISTENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar atualizar Sessão com dados em branco")
    void atualizarInvalid() throws Exception {
        // Arrange
        SessaoEstudoRequest invalidRequest = new SessaoEstudoRequest(null,null,null);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PATCH /monitor-estudos/sessoes/{id}/finalizar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK ao finalizar sessão em andamento")
    void finalizarSuccess() throws Exception {
        // Arrange
        SessaoEstudoResponse response = responsePadrao();

        when(sessaoEstudoService.finalizar(ID_SESSAO))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch(BASE_URL + "/{id}/finalizar", ID_SESSAO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_SESSAO))
                .andExpect(jsonPath("$.materiaId").value(ID_MATERIA));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar finalizar sessão já finalizada")
    void finalizarJaFinalizada() throws Exception {
        // Arrange
        when(sessaoEstudoService.finalizar(ID_SESSAO))
                .thenThrow(new SessaoJaFinalizadaException("A sessão já se encontra finalizada"));

        // Act & Assert
        mockMvc.perform(patch(BASE_URL + "/{id}/finalizar", ID_SESSAO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar finalizar sessão inexistente")
    void finalizarNotFound() throws Exception {
        // Arrange
        when(sessaoEstudoService.finalizar(ID_SESSAO_INEXISTENTE))
                .thenThrow(new EntityNotFoundException("Sessão não encontrada"));

        // Act & Assert
        mockMvc.perform(patch(BASE_URL + "/{id}/finalizar", ID_SESSAO_INEXISTENTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
