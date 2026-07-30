package com.toma.monitor_estudos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toma.monitor_estudos.dto.MateriaRequest;
import com.toma.monitor_estudos.dto.MateriaResponse;
import com.toma.monitor_estudos.service.MateriaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MateriaController.class)
class MateriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MateriaService materiaService;

    // -------------------------------------------------------------------------
    // Constantes e Helpers
    // -------------------------------------------------------------------------

    private static final String BASE_URL = "/monitor-estudos/materias";
    private static final Long ID_PADRAO = 1L;
    private static final String TITULO_PADRAO = "Java Backend";
    private static final String COR_PADRAO = "#FF0000";

    private static final Long ID_OUTRA = 2L;
    private static final String TITULO_OUTRA = "POO";
    private static final String COR_OUTRA = "#FF8888";

    private static final Long ID_INEXISTENTE = 99L;

    private MateriaRequest requestPadrao() {
        return new MateriaRequest(TITULO_PADRAO, COR_PADRAO);
    }

    private MateriaResponse responsePadrao() {
        return new MateriaResponse(ID_PADRAO, TITULO_PADRAO, COR_PADRAO);
    }

    // -------------------------------------------------------------------------
    // POST /monitor-estudos/materias
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 201 Created ao criar matéria com dados válidos")
    void criarMateriaSuccess() throws Exception {
        // Arrange
        MateriaRequest request = requestPadrao();
        MateriaResponse response = responsePadrao();

        when(materiaService.salvar(any(MateriaRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_PADRAO))
                .andExpect(jsonPath("$.titulo").value(TITULO_PADRAO))
                .andExpect(jsonPath("$.cor").value(COR_PADRAO));

        verify(materiaService).salvar(any(MateriaRequest.class));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar matéria com título em branco")
    void criarMateriaInvalid() throws Exception {
        // Arrange
        MateriaRequest invalidRequest = new MateriaRequest("", COR_PADRAO);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /monitor-estudos/materias
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK e a lista de matérias cadastradas")
    void listarMateriasSuccess() throws Exception {
        // Arrange
        MateriaResponse response = responsePadrao();
        MateriaResponse responseDois = new MateriaResponse(ID_OUTRA, TITULO_OUTRA, COR_OUTRA);

        when(materiaService.listarTodas())
                .thenReturn(List.of(response, responseDois));

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Ajustado para validar o tamanho numérico do array
                .andExpect(jsonPath("$[0].id").value(ID_PADRAO))
                .andExpect(jsonPath("$[0].titulo").value(TITULO_PADRAO))
                .andExpect(jsonPath("$[1].id").value(ID_OUTRA))
                .andExpect(jsonPath("$[1].titulo").value(TITULO_OUTRA));
    }

    @Test
    @DisplayName("Deve retornar 200 OK e a lista vazia")
    void listarMateriasVazia() throws Exception{
        when(materiaService.listarTodas())
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // -------------------------------------------------------------------------
    // DELETE /monitor-estudos/materias/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 204 No Content ao deletar matéria com ID existente")
    void deletar_DeveRetornar204_QuandoIdExistir() throws Exception {
        // Arrange
        doNothing().when(materiaService).deletar(ID_PADRAO);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", ID_PADRAO))
                .andExpect(status().isNoContent());

        verify(materiaService, times(1)).deletar(ID_PADRAO);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar deletar matéria com ID inexistente")
    void deletar_DeveRetornar404_QuandoIdNaoExistir() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Matéria não encontrada"))
                .when(materiaService).deletar(ID_INEXISTENTE);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", ID_INEXISTENTE))
                .andExpect(status().isNotFound());

        verify(materiaService, times(1)).deletar(ID_INEXISTENTE);
    }
}
